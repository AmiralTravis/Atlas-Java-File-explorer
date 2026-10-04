package atlas;

import com.sun.jna.platform.win32.Kernel32;

public class Main {
    public static void main(String[] args) {
        int pid = Kernel32.INSTANCE.GetCurrentProcessId();

        System.out.println("Process ID: " + pid);
    }
}