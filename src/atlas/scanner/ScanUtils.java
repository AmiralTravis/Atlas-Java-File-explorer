package atlas.scanner;

import atlas.AtlasState;

public class ScanUtils {
    

    /*
     * ============================================================
     * START SCAN
     * ============================================================
     */
    public static void startScan(
        AtlasState state,
        FileScanner fileScanner
    ) {

        state.cancelScanRequested.set(false);

        state.scanRunning.set(true);

        state.uiScanning = true;

        state.scanResult = null;


        ScanProgress progress =
            new ScanProgress();

        state.scanProgress =
            progress;


        Thread scanThread =
            new Thread(() -> {

                ScanResult result =
                    fileScanner.scan(
                        state.currentPath,
                        progress,
                        state.cancelScanRequested
                    );


                /*
                 * Scan thread ONLY updates state.
                 *
                 * It does NOT print anything.
                 */
                state.scanResult =
                    result;

                state.scanRunning.set(false);

            }, "Atlas-Scanner");


        scanThread.start();
    }

    /*
     * ============================================================
     * CANCEL SCAN
     * ============================================================
     */
    public static void cancelScan(
        AtlasState state
    ) {

        /*
         * Do NOT wait for the scanner here.
         *
         * We simply request cancellation.
         */
        state.cancelScanRequested.set(true);


        state.lastMessage =
            "Cancellation requested...";


        state.uiNeedsRender = true;
    }
    

    /*
     * ============================================================
     * PROGRESS
     * ============================================================
     */
    public static String showProgress(
        AtlasState state
    ) {

        if (state.scanProgress == null) {

            return "No scan progress available.";
        }


        return
            "Files Found: " +
            state.scanProgress.getFilesFound() +

            "\nFolders Found: " +
            state.scanProgress.getFoldersFound() +

            "\nSkipped: " +
            state.scanProgress.getSkippedFound() +

            "\nCurrent: " +
            state.scanProgress.getCurrentPath();
    }



}
