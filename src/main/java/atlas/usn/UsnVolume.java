package atlas.usn;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;


public class UsnVolume {

    public static void updateIndexByUsnJournal() {

        System.out.println("Proceeding to open volume");
        WinNT.HANDLE handle = openVolume("C");

        if (handle == null) {
            
            System.out.println("Error occured while opening volume\n");

            return;
        
        }

        System.out.println("Opened handle succesfully: " + handle);

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

}
