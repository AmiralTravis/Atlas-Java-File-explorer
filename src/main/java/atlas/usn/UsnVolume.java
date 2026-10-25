package atlas.usn;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Winioctl;
import com.sun.jna.platform.win32.WinioctlUtil;
import com.sun.jna.ptr.IntByReference;



import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UsnVolume {

    public static void updateIndexByUsnJournal() {

        System.out.println("Proceeding to open volume");
        WinNT.HANDLE handle = openVolume("C");

        if (handle == null) {
            
            System.out.println("Error occured while opening volume\n");

            return;
        
        }

        System.out.println("Opened handle succesfully: " + handle + "\n");

        System.out.println("Accessing usn journal...");
        USN_JOURNAL_DATA_V0 data = accessUsnJournal(handle);

        System.out.println("Accessed usn journal successfully\n");

        System.out.println("Accessing usn records...");
        accessUsnRecords(handle, data);

        System.out.println("Accessed usn records successfully\n");

        System.out.println("Closing handle");
        closeVolume(handle);
        System.out.println("Closed handle successfully...\n");
    }


    static WinNT.HANDLE openVolume(String driveLetter) {

        String volumePath = "\\\\.\\" + driveLetter + ":";

        WinNT.HANDLE handle = Kernel32.INSTANCE.CreateFile(
                volumePath,
                WinNT.GENERIC_READ | WinNT.GENERIC_WRITE,
                WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE,
                null,
                WinNT.OPEN_EXISTING,
                0,
                null
        );

        if (WinBase.INVALID_HANDLE_VALUE.equals(handle)) {

            int error = Kernel32.INSTANCE.GetLastError();

            if (error == 5) {
                System.out.println(
                    "Access denied. Please run Atlas as Administrator " +
                    "to use the USN Journal.\n"
                );

                return null;
            }

            throw new IllegalStateException(
                    "Failed to open volume " + volumePath +
                    ", error=" + error
            );
        }

        return handle;
    }


    static void closeVolume(WinNT.HANDLE handle) {

        boolean success = Kernel32.INSTANCE.CloseHandle(handle);

        if (!success) {
            throw new IllegalStateException(
                    "Failed to close volume handle" +
                    ", error=" + Kernel32.INSTANCE.GetLastError()
            );
        }
    
    }


    public static class USN_JOURNAL_DATA_V0 extends Structure {

        public long UsnJournalID;
        public long FirstUsn;
        public long NextUsn;
        public long LowestValidUsn;
        public long MaxUsn;
        public long MaximumSize;
        public long AllocationDelta;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                "UsnJournalID",
                "FirstUsn",
                "NextUsn",
                "LowestValidUsn",
                "MaxUsn",
                "MaximumSize",
                "AllocationDelta"
        
            );
        }
    }


    public static class READ_USN_JOURNAL_DATA_V0 extends Structure {

        public long StartUsn;
        public int ReasonMask;
        public int ReturnOnlyOnClose;
        public long Timeout;
        public long BytesToWaitFor;
        public long UsnJournalID;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                "StartUsn",
                "ReasonMask",
                "ReturnOnlyOnClose",
                "Timeout",
                "BytesToWaitFor",
                "UsnJournalID"
            );
        }
        
    }


    public static final int FSCTL_QUERY_USN_JOURNAL =
        WinioctlUtil.CTL_CODE(
                Winioctl.FILE_DEVICE_FILE_SYSTEM,
                61,
                Winioctl.METHOD_BUFFERED,
                Winioctl.FILE_ANY_ACCESS
        );


    public static final int FSCTL_READ_USN_JOURNAL =
        WinioctlUtil.CTL_CODE(
                Winioctl.FILE_DEVICE_FILE_SYSTEM,
                46,
                Winioctl.METHOD_NEITHER,
                Winioctl.FILE_ANY_ACCESS
        );



    static USN_JOURNAL_DATA_V0 accessUsnJournal(WinNT.HANDLE handle) {

        USN_JOURNAL_DATA_V0 data = new USN_JOURNAL_DATA_V0();

        IntByReference bytesReturned = new IntByReference();

        data.write(); // not necessary, just to make the jna read/write lifecycle explicit

        boolean success = Kernel32.INSTANCE.DeviceIoControl(
                handle,
                FSCTL_QUERY_USN_JOURNAL,
                Pointer.NULL,
                0,
                data.getPointer(),
                data.size(),
                bytesReturned,
                Pointer.NULL
        );

        if (!success) {
            int error = Kernel32.INSTANCE.GetLastError();
            throw new RuntimeException(
                    "FSCTL_QUERY_USN_JOURNAL failed. Error: " + error
            );
        }

        data.read(); // to read from native memory

        System.out.println(
                "Journal information received. Bytes: "
                + bytesReturned.getValue()
        );

        System.out.println("Journal raw data: " + data.getPointer());

        return data;

    }


    static void accessUsnRecords(WinNT.HANDLE handle, USN_JOURNAL_DATA_V0 journalData) {

        READ_USN_JOURNAL_DATA_V0 readData = new READ_USN_JOURNAL_DATA_V0();

        // USN_JOURNAL_DATA_V0 data = new USN_JOURNAL_DATA_V0();

        IntByReference bytesReturned = new IntByReference();

        readData.StartUsn = journalData.FirstUsn;
        readData.ReasonMask = 0xFFFFFFFF;
        readData.ReturnOnlyOnClose = 0;
        readData.Timeout = 0;
        readData.BytesToWaitFor = 0;
        readData.UsnJournalID = journalData.UsnJournalID;

        readData.write();


        // Memory outputBuffer = new Memory(64 * 1024);
        Memory outputBuffer = new Memory(51200 * 1024);


        // data.write(); // not necessary, just to make the jna read/write lifecycle explicit

        System.out.println("StartUsn       = " + readData.StartUsn);
        System.out.println("ReasonMask     = " + readData.ReasonMask);
        System.out.println("ReturnOnClose  = " + readData.ReturnOnlyOnClose);
        System.out.println("Timeout        = " + readData.Timeout);
        System.out.println("BytesToWaitFor = " + readData.BytesToWaitFor);
        System.out.println("JournalID      = " + readData.UsnJournalID);
        System.out.println("Struct size    = " + readData.size());

        boolean success = Kernel32.INSTANCE.DeviceIoControl(
                handle,
                FSCTL_READ_USN_JOURNAL,
                readData.getPointer(),
                readData.size(),
                outputBuffer,
                // outputBuffer.size(),
                (int) outputBuffer.size(),
                bytesReturned,
                Pointer.NULL
        );

        if (!success) {
            int error = Kernel32.INSTANCE.GetLastError();
            throw new RuntimeException(
                    "FSCTL_QUERY_USN_JOURNAL failed. Error: " + error
            );
        }

        readData.read(); // to read from native memory

        System.out.println(
                "Journal records information received. Bytes: "
                + bytesReturned.getValue()
        );

        System.out.println("Journal Records raw data: " + readData.getPointer());

        int bytes = bytesReturned.getValue();

        System.out.println("Bytes returned: " + bytes);
        System.out.println("Number of usn records read on avg: " + bytes/128);

        parseUsnRecords(outputBuffer, bytes);

    }





    
    

// Add these methods inside UsnVolume.

// static void parseUsnRecords(Memory buffer, int bytesReturned) {
//     long nextUsn = buffer.getLong(0);
//     System.out.println("Next USN: " + nextUsn);

//     long offset = 8;
//     int recordCount = 0;

//     while (offset + 8 <= bytesReturned && recordCount < 5) {
//         int length = buffer.getInt(offset);
//         short major = buffer.getShort(offset + 4);
//         short minor = buffer.getShort(offset + 6);

//         if (length < 8 || offset + length > bytesReturned) {
//             throw new IllegalStateException(
//                 "Invalid record length " + length + " at " + offset
//             );
//         }

//         recordCount++;

//         // System.out.println("\n========== USN RECORD #" + recordCount
//         //         + " ==========");
//         System.out.println("\n#" + recordCount);

//         // System.out.println("Record length: " + length + " bytes");
//         // System.out.println("Version: " + major + "." + minor);

//         if (major == 2 && length >= 60) {
//             long fileRef = buffer.getLong(offset + 8);
//             long parentRef = buffer.getLong(offset + 16);
//             long usn = buffer.getLong(offset + 24);
//             long fileTime = buffer.getLong(offset + 32);
//             int reason = buffer.getInt(offset + 40);
//             int sourceInfo = buffer.getInt(offset + 44);
//             long securityId = Integer.toUnsignedLong(
//                 buffer.getInt(offset + 48)
//             );
//             int attributes = buffer.getInt(offset + 52);
//             int nameLength = Short.toUnsignedInt(
//                 buffer.getShort(offset + 56)
//             );
//             int nameOffset = Short.toUnsignedInt(
//                 buffer.getShort(offset + 58)
//             );

//             if (nameOffset < 60
//                     || nameOffset + nameLength > length
//                     || nameLength % 2 != 0) {
//                 throw new IllegalStateException(
//                     "Invalid filename fields in record #" + recordCount
//                 );
//             }

//             byte[] nameBytes = buffer.getByteArray(
//                 offset + nameOffset, nameLength
//             );
//             String fileName = new String(
//                 nameBytes, java.nio.charset.StandardCharsets.UTF_16LE
//             );

//             // System.out.println("File name: " + fileName);
//             // System.out.println("USN: " + usn);
//             // System.out.println("File reference: " + fileRef
//             //         + " (0x" + Long.toHexString(fileRef) + ")");
//             // System.out.println("Parent reference: " + parentRef
//             //         + " (0x" + Long.toHexString(parentRef) + ")");
//             // System.out.println("Timestamp: " + formatFileTime(fileTime));
//             // System.out.println("Reason: 0x"
//             //         + String.format("%08X", reason)
//             //         + " " + decodeReason(reason));
//             System.out.println("Reason: " + decodeReason(reason));
//             // System.out.println("Source info: 0x"
//             //         + String.format("%08X", sourceInfo));
//             // System.out.println("Security ID: " + securityId);
//             // System.out.println("File attributes: 0x"
//             //         + String.format("%08X", attributes)
//             //         + " " + decodeAttributes(attributes));
//             // System.out.println("Filename length: " + nameLength + " bytes");
//             // System.out.println("Filename offset: " + nameOffset + " bytes");
//         } else {
//             System.out.println(
//                 "Detailed decoding not implemented for version "
//                 + major + "." + minor
//             );
//         }

//         offset += length;
//         offset = (offset + 7) & ~7L;
//     }

//     System.out.println("\nRecords displayed: " + recordCount);
// }



// static void parseUsnRecords(Memory buffer, int bytesReturned) {
//     long nextUsn = buffer.getLong(0);
//     System.out.println("Next USN: " + nextUsn);

//     long offset = 8;
//     int totalRecords = 0;
//     int displayedRecords = 0;

//     // Exact decoded reason combination -> number of records
//     Map<String, Integer> reasonCounts = new TreeMap<>();

//     while (offset + 8 <= bytesReturned) {
//         int length = buffer.getInt(offset);
//         short major = buffer.getShort(offset + 4);
//         short minor = buffer.getShort(offset + 6);

//         if (length < 8 || offset + length > bytesReturned) {
//             throw new IllegalStateException(
//                 "Invalid record length " + length + " at " + offset
//             );
//         }

//         totalRecords++;

//         if (major == 2 && length >= 60) {
//             int reason = buffer.getInt(offset + 40);
//             String decodedReason = decodeReason(reason);

//             // Count EVERY parsed V2 record, not just displayed records.
//             reasonCounts.merge(decodedReason, 1, Integer::sum);

//             // Display details for only the first 10 records.
//             if (displayedRecords < 3) {
//                 displayedRecords++;

//                 System.out.println("\n#" + displayedRecords);
//                 System.out.println("Reason: " + decodedReason);
//             }
//         } else if (displayedRecords < 3) {
//             displayedRecords++;

//             System.out.println("\n#" + displayedRecords);
//             System.out.println(
//                 "Detailed decoding not implemented for version "
//                 + major + "." + minor
//             );
//         }

//         // Move to the next 8-byte-aligned record.
//         offset = (offset + length + 7) & ~7L;
//     }

//     System.out.println("\nRecords displayed: " + displayedRecords);
//     System.out.println("Total records parsed: " + totalRecords);

//     System.out.println("\n=======================================");
//     System.out.println("USN REASON COMBINATION SUMMARY");
//     System.out.println("=======================================");

//     for (Map.Entry<String, Integer> entry : reasonCounts.entrySet()) {
//         System.out.printf("%-45s : %d%n",
//             entry.getKey(), entry.getValue());
//     }

//     System.out.println("=======================================");
//     System.out.println(
//         "Distinct combinations: " + reasonCounts.size()
//     );
// }




// static void parseUsnRecords(Memory buffer, int bytesReturned) {
//     long nextUsn = buffer.getLong(0);
//     System.out.println("Next USN: " + nextUsn);

//     long offset = 8;
//     int totalRecords = 0;
//     int displayedRecords = 0;

//     Map<String, Integer> reasonCounts = new TreeMap<>();

//     // USN reason flags
//     final int RENAME_OLD_NAME = 0x00001000;
//     final int RENAME_NEW_NAME = 0x00002000;
//     final int RENAME_MASK = RENAME_OLD_NAME | RENAME_NEW_NAME;

//     while (offset + 8 <= bytesReturned) {
//         int length = buffer.getInt(offset);
//         short major = buffer.getShort(offset + 4);
//         short minor = buffer.getShort(offset + 6);

//         if (length < 8 || offset + length > bytesReturned) {
//             throw new IllegalStateException(
//                 "Invalid record length " + length + " at " + offset
//             );
//         }

//         totalRecords++;

//         if (major == 2 && length >= 60) {
//             long fileRef = buffer.getLong(offset + 8);
//             long parentRef = buffer.getLong(offset + 16);
//             long usn = buffer.getLong(offset + 24);
//             long fileTime = buffer.getLong(offset + 32);
//             int reason = buffer.getInt(offset + 40);
//             int sourceInfo = buffer.getInt(offset + 44);
//             long securityId = Integer.toUnsignedLong(
//                 buffer.getInt(offset + 48)
//             );
//             int attributes = buffer.getInt(offset + 52);
//             int nameLength = Short.toUnsignedInt(
//                 buffer.getShort(offset + 56)
//             );
//             int nameOffset = Short.toUnsignedInt(
//                 buffer.getShort(offset + 58)
//             );

//             String decodedReason = decodeReason(reason);

//             // Count all V2 records, whether displayed or not.
//             reasonCounts.merge(decodedReason, 1, Integer::sum);

//             byte[] nameBytes = buffer.getByteArray(
//                     offset + nameOffset, nameLength
//                 );

//             String fileName = new String(
//                     nameBytes,
//                     java.nio.charset.StandardCharsets.UTF_16LE
//                 );


//             // if (fileName.toLowerCase(java.util.Locale.ROOT)
//             //        .contains("pass")) {

//             if (fileName.equalsIgnoreCase("laptops shortlist.txt")) {


//                 if (nameOffset < 60
//                         || nameOffset + nameLength > length
//                         || nameLength % 2 != 0) {
//                     throw new IllegalStateException(
//                         "Invalid filename fields in record #" + totalRecords
//                     );
//                 }

                

                

//                 displayedRecords++;

//                 System.out.println("\n#" + totalRecords);
//                 System.out.println("File name:         " + fileName);
//                 // System.out.println("File reference ID: " + fileRef
//                 //     + " (0x" + Long.toHexString(fileRef) + ")");
//                 // System.out.println("Parent reference:  " + parentRef
//                 //     + " (0x" + Long.toHexString(parentRef) + ")");
//                 // System.out.println("USN:               " + usn);
//                 // System.out.println("Timestamp:         " + formatFileTime(fileTime));
//                 System.out.println("Reason:            " + decodedReason);
//                 // System.out.printf("Reason flags:      0x%08X%n", reason);
//                 // System.out.printf("Source info:       0x%08X%n", sourceInfo);
//                 // System.out.println("Security ID:       " + securityId);
//                 // System.out.printf("File attributes:   0x%08X (%s)%n",
//                     // attributes, decodeAttributes(attributes));
//                 // System.out.println("Record version:    " + major + "." + minor);
//                 // System.out.println("Record length:     " + length + " bytes");
//             }
//         }

//         // Move to the next 8-byte-aligned record.
//         offset = (offset + length + 7) & ~7L;
//     }

//     System.out.println("\nRecords displayed (rename-related): " + displayedRecords);
//     System.out.println("Total records parsed: " + totalRecords);

//     // System.out.println("\n=======================================");
//     // System.out.println("USN REASON COMBINATION SUMMARY");
//     // System.out.println("=======================================");

//     // for (Map.Entry<String, Integer> entry : reasonCounts.entrySet()) {
//     //     System.out.printf("%-45s : %d%n",
//     //         entry.getKey(), entry.getValue());
//     // }

//     // System.out.println("=======================================");
//     // System.out.println("Distinct combinations: " + reasonCounts.size());
// }



static void parseUsnRecords(Memory buffer, int bytesReturned) {
    long nextUsn = buffer.getLong(0);
    System.out.println("Next USN: " + nextUsn);

    long offset = 8;
    int totalRecords = 0;

    long earliestTime = Long.MAX_VALUE;
    long latestTime = Long.MIN_VALUE;

    String earliestRecord = null;
    String latestRecord = null;

    Map<String, Integer> reasonCounts = new TreeMap<>();

    while (offset + 8 <= bytesReturned) {
        int length = buffer.getInt(offset);
        short major = buffer.getShort(offset + 4);
        short minor = buffer.getShort(offset + 6);

        if (length < 8 || offset + length > bytesReturned) {
            throw new IllegalStateException(
                "Invalid record length " + length + " at " + offset
            );
        }

        totalRecords++;

        if (major == 2 && length >= 60) {
            long fileRef = buffer.getLong(offset + 8);
            long parentRef = buffer.getLong(offset + 16);
            long usn = buffer.getLong(offset + 24);
            long fileTime = buffer.getLong(offset + 32);
            int reason = buffer.getInt(offset + 40);
            int sourceInfo = buffer.getInt(offset + 44);
            long securityId = Integer.toUnsignedLong(
                buffer.getInt(offset + 48)
            );
            int attributes = buffer.getInt(offset + 52);
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

            String fileName = new String(
                buffer.getByteArray(offset + nameOffset, nameLength),
                java.nio.charset.StandardCharsets.UTF_16LE
            );

            String recordInfo =
                "Record #: " + totalRecords
                + "\nFile name: " + fileName
                + "\nFile reference ID: " + fileRef
                + " (0x" + Long.toHexString(fileRef) + ")"
                + "\nParent reference ID: " + parentRef
                + " (0x" + Long.toHexString(parentRef) + ")"
                + "\nUSN: " + usn
                + "\nTimestamp: " + formatFileTime(fileTime)
                + "\nReason: " + decodedReason
                + String.format("%nReason flags: 0x%08X", reason)
                + String.format("%nSource info: 0x%08X", sourceInfo)
                + "\nSecurity ID: " + securityId
                + String.format("%nFile attributes: 0x%08X (%s)",
                    attributes, decodeAttributes(attributes))
                + "\nRecord version: " + major + "." + minor
                + "\nRecord length: " + length + " bytes";

            if (fileTime < earliestTime) {
                earliestTime = fileTime;
                earliestRecord = recordInfo;
            }

            if (fileTime > latestTime) {
                latestTime = fileTime;
                latestRecord = recordInfo;
            }
        }

        offset = (offset + length + 7) & ~7L;
    }

    System.out.println("\n========== EARLIEST RECORD ==========");
    System.out.println(
        earliestRecord != null ? earliestRecord : "No V2 records found"
    );

    System.out.println("\n========== LATEST RECORD ==========");
    System.out.println(
        latestRecord != null ? latestRecord : "No V2 records found"
    );

    System.out.println("\nTotal records parsed: " + totalRecords);

    // System.out.println("\n=======================================");
    // System.out.println("USN REASON COMBINATION SUMMARY");
    // System.out.println("=======================================");

    // for (Map.Entry<String, Integer> entry : reasonCounts.entrySet()) {
    //     System.out.printf("%-45s : %d%n",
    //         entry.getKey(), entry.getValue());
    // }

    // System.out.println("=======================================");
    // System.out.println(
    //     "Distinct combinations: " + reasonCounts.size()
    // );
}





static String formatFileTime(long fileTime) {
    // Windows FILETIME: 100-nanosecond intervals since Jan 1, 1601.
    long epochMillis = (fileTime - 116444736000000000L) / 10000L;
    return Instant.ofEpochMilli(epochMillis).toString();
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

static String decodeAttributes(int attributes) {
    StringBuilder result = new StringBuilder();

    addFlag(result, attributes, 0x00000001, "READONLY");
    addFlag(result, attributes, 0x00000002, "HIDDEN");
    addFlag(result, attributes, 0x00000004, "SYSTEM");
    addFlag(result, attributes, 0x00000010, "DIRECTORY");
    addFlag(result, attributes, 0x00000020, "ARCHIVE");
    addFlag(result, attributes, 0x00000080, "NORMAL");
    addFlag(result, attributes, 0x00000100, "TEMPORARY");
    addFlag(result, attributes, 0x00000200, "SPARSE_FILE");
    addFlag(result, attributes, 0x00000400, "REPARSE_POINT");
    addFlag(result, attributes, 0x00000800, "COMPRESSED");
    addFlag(result, attributes, 0x00001000, "OFFLINE");
    addFlag(result, attributes, 0x00002000, "NOT_CONTENT_INDEXED");
    addFlag(result, attributes, 0x00004000, "ENCRYPTED");

    return result.length() == 0 ? "OTHER/NO FLAGS" : result.toString();
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
