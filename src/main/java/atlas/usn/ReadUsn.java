package atlas.usn;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Winioctl;
import com.sun.jna.platform.win32.WinioctlUtil;
import com.sun.jna.ptr.IntByReference;

import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;


public class ReadUsn {


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






    public static UsnUpdateRecords getUsnUpdates(
        ArrayList<String> targetDirectories,
        long lastProcessedUsn
    ) {

        System.out.println("Proceeding to open volume");
        WinNT.HANDLE handle = openVolume("C");

        if (handle == null) {
            
            // System.out.println("Error occured while opening volume\n");

            return null;
        
        }

        // System.out.println("Opened handle succesfully: " + handle + "\n");

        // System.out.println("Accessing usn journal...");
        USN_JOURNAL_DATA_V0 data = accessUsnJournal(handle);

        // System.out.println("Accessed usn journal successfully\n");

        // System.out.println("Accessing usn records...");
        UsnUpdateRecords updateRecord = accessAndReturnUsnRecords(
            targetDirectories, handle, data, lastProcessedUsn);

        // System.out.println("Accessed usn records successfully\n");

        // System.out.println("Closing handle");
        closeVolume(handle);
        // System.out.println("Closed handle successfully...\n");

        return updateRecord;
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
                // System.out.println(
                //     "Access denied. Please run Atlas as Administrator " +
                //     "to use the USN Journal.\n"
                // );

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

        // System.out.println(
        //         "Journal information received. Bytes: "
        //         + bytesReturned.getValue()
        // );

        // System.out.println("Journal raw data: " + data.getPointer());

        return data;

    }


    static UsnUpdateRecords accessAndReturnUsnRecords(
        ArrayList<String> targetDirectories,
        WinNT.HANDLE handle, USN_JOURNAL_DATA_V0 journalData,
        long lastProcessedUsn
    ) {



    
        // long lastProcessedUsn = 20781506728L; //old
        // long lastProcessedUsn = 20786679512L; //new

        READ_USN_JOURNAL_DATA_V0 readData = new READ_USN_JOURNAL_DATA_V0();

        IntByReference bytesReturned = new IntByReference();

        // readData.StartUsn = journalData.FirstUsn;
        readData.StartUsn = (lastProcessedUsn == 0)
            ? journalData.FirstUsn
            : lastProcessedUsn;
        readData.ReasonMask = 0xFFFFFFFF;
        readData.ReturnOnlyOnClose = 0;
        readData.Timeout = 0;
        readData.BytesToWaitFor = 0;
        readData.UsnJournalID = journalData.UsnJournalID;

        readData.write();


        Memory outputBuffer = new Memory(51200 * 1024);


        // data.write(); // not necessary, just to make the jna read/write lifecycle explicit

        // System.out.println("StartUsn       = " + readData.StartUsn);
        // System.out.println("ReasonMask     = " + readData.ReasonMask);
        // System.out.println("ReturnOnClose  = " + readData.ReturnOnlyOnClose);
        // System.out.println("Timeout        = " + readData.Timeout);
        // System.out.println("BytesToWaitFor = " + readData.BytesToWaitFor);
        // System.out.println("JournalID      = " + readData.UsnJournalID);
        // System.out.println("Struct size    = " + readData.size());

        boolean success = Kernel32.INSTANCE.DeviceIoControl(
                handle,
                FSCTL_READ_USN_JOURNAL,
                readData.getPointer(),
                readData.size(),
                outputBuffer,
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

        // System.out.println(
        //         "Journal records information received. Bytes: "
        //         + bytesReturned.getValue()
        // );

        // System.out.println("Journal Records raw data: " + readData.getPointer());

        int bytes = bytesReturned.getValue();

        // System.out.println("Bytes returned: " + bytes);
        // System.out.println("Number of usn records read on avg: " + bytes/128);


        UsnUpdateRecords updateRecord = ParseUsn.parseAndReturnUsnRecords(
            targetDirectories, outputBuffer, bytes, handle
        );

        if (bytes >= 8) {
            lastProcessedUsn = outputBuffer.getLong(0);
        }

        System.out.println("lastProcessedUsnn: " + lastProcessedUsn);

        updateRecord.addLastProcessedUsn(updateRecord, lastProcessedUsn);

        return updateRecord;
    }







    // HELPER METHOD FOR OTHER FILES AND PACKAGES

    public static long getLastRecordedUsn() {

        WinNT.HANDLE handle = openVolume("C");

        if (handle == null) {
            throw new IllegalStateException("Failed to open volume C:");
        }

        try {

            USN_JOURNAL_DATA_V0 data = accessUsnJournal(handle);

            return data.NextUsn;

        } finally {
            closeVolume(handle);
        }
    }


    public static class BY_HANDLE_FILE_INFORMATION extends Structure {

    public WinNT.DWORD dwFileAttributes;
    public WinNT.FILETIME ftCreationTime;
    public WinNT.FILETIME ftLastAccessTime;
    public WinNT.FILETIME ftLastWriteTime;
    public WinNT.DWORD dwVolumeSerialNumber;
    public WinNT.DWORD nFileSizeHigh;
    public WinNT.DWORD nFileSizeLow;
    public WinNT.DWORD nNumberOfLinks;
    public WinNT.DWORD nFileIndexHigh;
    public WinNT.DWORD nFileIndexLow;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
            "dwFileAttributes",
            "ftCreationTime",
            "ftLastAccessTime",
            "ftLastWriteTime",
            "dwVolumeSerialNumber",
            "nFileSizeHigh",
            "nFileSizeLow",
            "nNumberOfLinks",
            "nFileIndexHigh",
            "nFileIndexLow"
        );
    }
}
    





public interface Kernel32Extended extends StdCallLibrary {

    Kernel32Extended INSTANCE =
        com.sun.jna.Native.load(
            "kernel32",
            Kernel32Extended.class,
            W32APIOptions.DEFAULT_OPTIONS
        );

    boolean GetFileInformationByHandle(
        WinNT.HANDLE hFile,
        BY_HANDLE_FILE_INFORMATION lpFileInformation
    );
}




public static long getFileId(Path path) {

    WinNT.HANDLE handle = Kernel32.INSTANCE.CreateFile(
        path.toAbsolutePath().toString(),
        WinNT.FILE_READ_ATTRIBUTES,
        WinNT.FILE_SHARE_READ |
        WinNT.FILE_SHARE_WRITE |
        WinNT.FILE_SHARE_DELETE,
        null,
        WinNT.OPEN_EXISTING,
        WinNT.FILE_FLAG_BACKUP_SEMANTICS,
        null
    );

    if (WinBase.INVALID_HANDLE_VALUE.equals(handle)) {
        throw new IllegalStateException(
            "Could not open: " + path +
            ", error=" + Kernel32.INSTANCE.GetLastError()
        );
    }

    try {

        BY_HANDLE_FILE_INFORMATION info =
            new BY_HANDLE_FILE_INFORMATION();

        // boolean success =
        //     Kernel32.INSTANCE.GetFileInformationByHandle(
        //         handle,
        //         info
        //     );
        boolean success =
            Kernel32Extended.INSTANCE.GetFileInformationByHandle(
                handle,
                info
            );

        if (!success) {
            throw new IllegalStateException(
                "GetFileInformationByHandle failed. Error: " +
                Kernel32.INSTANCE.GetLastError()
            );
        }

        info.read();

        return
            ((info.nFileIndexHigh.longValue() & 0xFFFFFFFFL) << 32)
            |
            (info.nFileIndexLow.longValue() & 0xFFFFFFFFL);

    } finally {
        Kernel32.INSTANCE.CloseHandle(handle);
    }
}


public static long getParentFileId(Path path) {

    Path parent = path.toAbsolutePath().getParent();

    if (parent == null) {
        throw new IllegalArgumentException(
            "Path has no parent: " + path
        );
    }

    return getFileId(parent);
}



    
}
