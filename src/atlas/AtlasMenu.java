package atlas;

import java.awt.Desktop;
import java.io.File;

import atlas.scanner.FileScanner;
import atlas.scanner.ScanProgress;
import atlas.scanner.ScanResult;

public class AtlasMenu {

    public static void show(AtlasState state) {

        if (
            !state.scanRunning.get()
            && state.scanResult != null
        ) {

            printScanResult(state);

            return;
        }

        if (state.scanRunning.get()) {

            System.out.println(
                "\nScanner is still running...\n" +
                "Scanner options:\n" +
                "progress\n" +
                "cancel\n"
            );

        } else {

            System.out.println(
                "\nCurrent Path: " +
                state.currentPath +
                "\n"
            );

            System.out.println("Menu:");

            System.out.println(
                "show root\n" +
                "show current\n" +
                "open {name}\n" +
                "parent dir\n" +
                "scan\n" +
                "exit\n"
            );
        }
    }


    public static String handleCommand(
        String input,
        AtlasState state,
        FileScanner fileScanner
    ) {

        /*
         * Scanner mode
         */
        if (state.scanRunning.get()) {

            return handleScannerCommand(
                input,
                state
            );
        }


        /*
         * Normal Atlas mode
         */
        return handleNormalCommand(
            input,
            state,
            fileScanner
        );
    }


    private static String handleScannerCommand(
        String input,
        AtlasState state
    ) {

        if (input.equals("progress")) {

            return showProgress(state);
        }

        // else if (input.equals("cancel")) {

        //     state.cancelScanRequested.set(true);

        //     return "Cancellation requested...";
        // }
        else if (input.equals("cancel")) {

            cancelScan(state);

            return "";
        }

        else {

            return "Scanner is still running. Use 'progress' or 'cancel'.";
        }
    }


    private static String handleNormalCommand(
        String input,
        AtlasState state,
        FileScanner fileScanner
    ) {

        if (input.equals("show root")) {

            showRoot();

            return "";
        }

        else if (input.equals("show current")) {

            showCurrent(state);

            return "";
        }

        else if (input.startsWith("open ")) {

            String itemName = input.substring(5);

            openItem(itemName, state);

            return "";
        }

        else if (input.equals("parent dir")) {

            parentDir(state);

            return "";
        }

        else if (input.equals("scan")) {

            startScan(state, fileScanner);

            return "Scanner started.";
        }

        else if (input.equals("exit")) {

            return "exit";
        }

        else {

            return "Invalid input.";
        }
    }


    private static void startScan(
        AtlasState state,
        FileScanner fileScanner
    ) {

        state.cancelScanRequested.set(false);
        state.scanRunning.set(true);

        ScanProgress progress = new ScanProgress();

        state.scanProgress = progress;

        Thread scanThread = new Thread(() -> {

            ScanResult result = fileScanner.scan(
                state.currentPath,
                progress,
                state.cancelScanRequested
            );

            state.scanResult = result;

            state.scanRunning.set(false);
        });

        scanThread.start();
    }


    private static String showProgress(
        AtlasState state
    ) {

        return
            // "\nScanning...\n" +
            "Files Found: " +
            state.scanProgress.getFilesFound() +
            "\nFolders Found: " +
            state.scanProgress.getFoldersFound() +
            "\nCurrent: " +
            state.scanProgress.getCurrentPath();
    }


    private static void showRoot() {

        File directory = new File("\\");

        File[] items = directory.listFiles();

        if (items == null) {

            System.out.println("Unable to access root directory.");

            return;
        }

        for (File item : items) {

            if (item.isFile()) {

                System.out.println(item.getName());
            }

            else if (item.isDirectory()) {

                System.out.println("\\" + item.getName());
            }
        }
    }


    private static void showCurrent(
        AtlasState state
    ) {

        File directory = state.currentPath.toFile();

        File[] items = directory.listFiles();

        if (items == null) {

            System.out.println(
                "Unable to access: " + state.currentPath
            );

            return;
        }

        for (File item : items) {

            if (item.isFile()) {

                System.out.println(item.getName());
            }

            else if (item.isDirectory()) {

                System.out.println("\\" + item.getName());
            }
        }
    }


    private static void openItem(
        String itemName,
        AtlasState state
    ) {

        File item = state.currentPath
            .resolve(itemName)
            .toFile();

        if (!item.exists()) {

            System.err.println(
                "This item doesn't exist in this directory."
            );

            return;
        }


        if (item.isFile()) {

            try {

                Desktop.getDesktop().open(item);

            } catch (Exception e) {

                System.err.println(
                    "Error opening file " +
                    item +
                    ": " +
                    e.getMessage()
                );
            }

            return;
        }


        if (item.isDirectory()) {

            state.currentPath = item.toPath();

        }
    }


    private static void parentDir(
        AtlasState state
    ) {

        if (state.currentPath.getParent() != null) {

            state.currentPath =
                state.currentPath.getParent();

        } else {

            System.out.println(
                "Already at root dir!"
            );
        }
    }



    private static void cancelScan(AtlasState state) {

        state.cancelScanRequested.set(true);

        System.out.println(
            "Cancellation requested..."
        );

        while (state.scanRunning.get()) {

            try {

                Thread.sleep(50);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                return;
            }
        }

        printScanResult(state);
    }



    private static void printScanResult(
        AtlasState state
    ) {

        if (state.scanResult == null) {
            return;
        }

        System.out.println();

        if (state.cancelScanRequested.get()) {

            System.out.println(
                "Scan cancelled."
            );

        } else {

            System.out.println(
                "Scan complete."
            );
        }

        System.out.println(
            "Files Found: " +
            state.scanResult.getFilesFound()
        );

        System.out.println(
            "Folders Found: " +
            state.scanResult.getFoldersFound()
        );

        System.out.println();

        state.scanResult = null;
    }




}