package atlas.usn;

import com.sun.jna.Native;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import atlas.scanner.Index;
import atlas.scanner.FileSystemNode;

public class ParseUsn {
    
    


static UsnUpdateRecords parseAndReturnUsnRecords(
    ArrayList<String> targetDirectories,
    Memory buffer,
    int bytesReturned,
    WinNT.HANDLE volumeHandle,
    Index atlasIndex
) {



    long offset = 8;
    int totalRecords = 0;

    UsnUpdateRecords updates = new UsnUpdateRecords();

    Map<String, Integer> reasonCounts = new TreeMap<>();

    while (offset + 8 <= bytesReturned) {

        int length = buffer.getInt(offset);
        short major = buffer.getShort(offset + 4);

        if (length < 8 || offset + length > bytesReturned) {
            throw new IllegalStateException(
                "Invalid record length " + length + " at " + offset
            );
        }

        totalRecords++;

        if (major == 2 && length >= 60) {

            long fileRef = buffer.getLong(offset + 8);
            long parentFileRef = buffer.getLong(offset + 16);

            int reason = buffer.getInt(offset + 40);



            int nameLength = Short.toUnsignedInt(
                buffer.getShort(offset + 56)
            );

            int nameOffset = Short.toUnsignedInt(
                buffer.getShort(offset + 58)
            );

            String decodedReason = decodeReason(reason);

            reasonCounts.merge(decodedReason, 1, Integer::sum);

            if (nameOffset < 60
                    || nameOffset + nameLength > length
                    || nameLength % 2 != 0) {

                throw new IllegalStateException(
                    "Invalid filename fields in record #" + totalRecords
                );
            }

            



            String fullPath = getFullPathByFileId(volumeHandle, fileRef);

            if (fullPath == null || fullPath.isBlank()) {
                fullPath = "Unable to find";
            }

            // DELETE HANDLING

            if (
                (
                    fullPath.equals("Unable to find") &&
                    decodedReason.contains("FILE_DELETE")
                )   
                ||
                (
                    decodedReason.contains("RENAME") &&
                    fullPath.startsWith("\\\\?\\C:\\$Recycle.Bin")
                )
            ) {

                FileSystemNode existingNode = atlasIndex.getNode(fileRef);

                if (existingNode != null) {

                    UsnRecord record = new UsnRecord(
                        null,
                        null,
                        decodedReason,
                        "Deleted file",
                        fileRef,
                        parentFileRef
                    );

                    updates.addRecord(record);
                }

                // Do not continue with normal attribute reading.
                offset = (offset + length + 7) & ~7L;
                continue;
            }

            

            // Map the USN reason to one of the four categories.
            String mapping = mapReason(reason);
           

            // Include records whose paths could not be resolved.
            // These cannot be checked against the directory filter.
            

            // Normalize the resolved path.
            String normalizedPath = fullPath
                .replace('/', '\\')
                .replaceFirst("^\\\\\\\\\\?\\\\", "");

            boolean directoryMatched = false;

            for (String directory : targetDirectories) {

                String normalizedDirectory = directory
                    .replace('/', '\\')
                    .replaceFirst("^\\\\\\\\\\?\\\\", "");

                // Remove trailing separators, except for drive roots.
                while (normalizedDirectory.endsWith("\\")
                        && normalizedDirectory.length() > 3) {
                    normalizedDirectory = normalizedDirectory.substring(
                        0, normalizedDirectory.length() - 1
                    );
                }

                String directoryPrefix =
                    normalizedDirectory.endsWith("\\")
                        ? normalizedDirectory
                        : normalizedDirectory + "\\";

                // Match the directory itself or anything beneath it.
                if (normalizedPath.equalsIgnoreCase(normalizedDirectory)
                        || normalizedPath.regionMatches(
                            true, 0, directoryPrefix, 0,
                            directoryPrefix.length()
                        )) {

                    directoryMatched = true;
                    break;
                }
            }

            if (directoryMatched) {


                // Construct UsnRecord if the path is resolved
                if (!fullPath.equals("Unable to find")) {

                    Path path = Path.of(normalizedPath);

                    try {
                        BasicFileAttributes fileAttributes  =
                            Files.readAttributes(path, BasicFileAttributes.class);

                        
                        UsnRecord record = new UsnRecord(
                            path,
                            fileAttributes,
                            decodedReason,
                            mapping != null ? mapping : "Other change",
                            fileRef,
                            parentFileRef
                        );

                        updates.addRecord(record);

                    } catch (IOException e) {
                        System.out.println("Could not read attributes: " + fullPath);
                    }
                }
            }
            
        }

        // Move to the next 8-byte-aligned record.
        offset = (offset + length + 7) & ~7L;
    }



    return updates;

}


private interface Kernel32Path extends StdCallLibrary {

    Kernel32Path INSTANCE = Native.load(
        "kernel32",
        Kernel32Path.class,
        W32APIOptions.UNICODE_OPTIONS
    );

    WinNT.HANDLE OpenFileById(
        WinNT.HANDLE hVolumeHint,
        Pointer lpFileId,
        int dwDesiredAccess,
        int dwShareMode,
        Pointer lpSecurityAttributes,
        int dwFlagsAndAttributes
    );

    int GetFinalPathNameByHandleW(
        WinNT.HANDLE hFile,
        char[] lpszFilePath,
        int cchFilePath,
        int dwFlags
    );
}

static String getFullPathByFileId(
        WinNT.HANDLE volumeHandle, long fileRef) {

    // FILE_ID_DESCRIPTOR: 24 bytes
    Memory fileIdDescriptor = new Memory(24);
    fileIdDescriptor.clear();

    fileIdDescriptor.setInt(0, 24);     // dwSize
    fileIdDescriptor.setInt(4, 0);      // FileIdType
    fileIdDescriptor.setLong(8, fileRef); // FileId

    WinNT.HANDLE fileHandle = Kernel32Path.INSTANCE.OpenFileById(
        volumeHandle,
        fileIdDescriptor,
        0x0080,          // FILE_READ_ATTRIBUTES
        0x00000007,      // SHARE_READ | SHARE_WRITE | SHARE_DELETE
        null,
        0x02000000       // FILE_FLAG_BACKUP_SEMANTICS
    );

    if (fileHandle == null ||
            WinBase.INVALID_HANDLE_VALUE.equals(fileHandle)) {
        return null;
    }

    try {

        char[] pathBuffer = new char[32768];

        int length = Kernel32Path.INSTANCE.GetFinalPathNameByHandleW(
            fileHandle,
            pathBuffer,
            pathBuffer.length,
            0
        );

        if (length == 0 || length >= pathBuffer.length) {
            return null;
        }

        return Native.toString(pathBuffer);

    } finally {
        Kernel32.INSTANCE.CloseHandle(fileHandle);

    }
}





static String formatFileTime(long fileTime) {
    long epochMillis =
        (fileTime - 116444736000000000L) / 10000L;

    return Instant.ofEpochMilli(epochMillis)
        .atZone(java.time.ZoneId.of("Asia/Kolkata"))
        .format(
            java.time.format.DateTimeFormatter
                .ofPattern("dd-MM-yyyy hh:mm:ss a")
        );
}

static String decodeReason(int reason) {
    StringBuilder result = new StringBuilder();

    addFlag(result, reason, 0x00000001, "DATA_OVERWRITE");
    addFlag(result, reason, 0x00000002, "DATA_EXTEND");
    addFlag(result, reason, 0x00000004, "DATA_TRUNCATION");
    addFlag(result, reason, 0x00000010, "NAMED_DATA_OVERWRITE");
    addFlag(result, reason, 0x00000020, "NAMED_DATA_EXTEND");
    addFlag(result, reason, 0x00000040, "NAMED_DATA_TRUNCATION");
    addFlag(result, reason, 0x00000100, "FILE_CREATE");
    addFlag(result, reason, 0x00000200, "FILE_DELETE");
    addFlag(result, reason, 0x00000400, "EA_CHANGE");
    addFlag(result, reason, 0x00000800, "SECURITY_CHANGE");
    addFlag(result, reason, 0x00001000, "RENAME_OLD_NAME");
    addFlag(result, reason, 0x00002000, "RENAME_NEW_NAME");
    addFlag(result, reason, 0x00004000, "INDEXABLE_CHANGE");
    addFlag(result, reason, 0x00008000, "BASIC_INFO_CHANGE");
    addFlag(result, reason, 0x00010000, "HARD_LINK_CHANGE");
    addFlag(result, reason, 0x00020000, "COMPRESSION_CHANGE");
    addFlag(result, reason, 0x00040000, "ENCRYPTION_CHANGE");
    addFlag(result, reason, 0x00080000, "OBJECT_ID_CHANGE");
    addFlag(result, reason, 0x00100000, "REPARSE_POINT_CHANGE");
    addFlag(result, reason, 0x00200000, "STREAM_CHANGE");
    addFlag(result, reason, 0x00400000, "TRANSACTED_CHANGE");
    addFlag(result, reason, 0x00800000, "INTEGRITY_CHANGE");
    addFlag(result, reason, 0x80000000, "CLOSE");

    return result.length() == 0 ? "UNKNOWN/NO FLAGS" : result.toString();

}




static String mapReason(int reason) {

    if ((reason & 0x00000100) != 0)
        return "New file";

    if ((reason & 0x00000200) != 0)
        return "Deleted file";

    if ((reason & 0x00003000) != 0)
        return "Renamed file";

    int contentFlags =
        0x00000001 |  // DATA_OVERWRITE
        0x00000002 |  // DATA_EXTEND
        0x00000004 |  // DATA_TRUNCATION
        0x00000010 |  // NAMED_DATA_OVERWRITE
        0x00000020 |  // NAMED_DATA_EXTEND
        0x00000040 |  // NAMED_DATA_TRUNCATION
        0x00000400 |  // EA_CHANGE
        0x00000800 |  // SECURITY_CHANGE
        0x00004000 |  // INDEXABLE_CHANGE
        0x00008000 |  // BASIC_INFO_CHANGE
        0x00010000 |  // HARD_LINK_CHANGE
        0x00020000 |  // COMPRESSION_CHANGE
        0x00040000 |  // ENCRYPTION_CHANGE
        0x00080000 |  // OBJECT_ID_CHANGE
        0x00100000 |  // REPARSE_POINT_CHANGE
        0x00200000 |  // STREAM_CHANGE
        0x00400000 |  // TRANSACTED_CHANGE
        0x00800000;   // INTEGRITY_CHANGE

    if ((reason & contentFlags) != 0)
        return "Updated file contents";

    return null;
}




static void addFlag(
        StringBuilder result, int value, int flag, String name) {
    if ((value & flag) != 0) {
        if (result.length() > 0) {
            result.append(" | ");
        }
        result.append(name);
    }
}




}
