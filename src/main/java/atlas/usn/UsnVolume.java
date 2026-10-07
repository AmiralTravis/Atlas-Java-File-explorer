package atlas.usn;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Winioctl;
import com.sun.jna.platform.win32.WinioctlUtil;
import com.sun.jna.ptr.IntByReference;



public class UsnVolume {

    public static void updateIndexByUsnJournal() {

        System.out.println("Proceeding to open volume");
        WinNT.HANDLE handle = openVolume("C");

        if (handle == null) {
            
            System.out.println("Error occured while opening volume\n");

            return;
        
        }

        System.out.println("Opened handle succesfully: " + handle);

        System.out.println("Accessing usn journal...");
        accessUsnJournal(handle);

        System.out.println("Accessed usn journal successfully");

        System.out.println("Closing handle");
        closeVolume(handle);
        System.out.println("Closed handle successfully...");
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

    public static final int FSCTL_QUERY_USN_JOURNAL =
        WinioctlUtil.CTL_CODE(
                Winioctl.FILE_DEVICE_FILE_SYSTEM,
                61,
                Winioctl.METHOD_BUFFERED,
                Winioctl.FILE_ANY_ACCESS
        );



    static void accessUsnJournal(WinNT.HANDLE handle) {

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
    }
}
