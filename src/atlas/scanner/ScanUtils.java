package atlas.scanner;


import atlas.AtlasState;

public class ScanUtils {
    

    /*
     * ============================================================
     * START SCAN
     * ============================================================
     */
    public static void startScan(
        AtlasState state
    ) {

        state.currentScanPath = state.currentPath;

        state.currentScanResult = new ScanResult();
        
        ScanResult result = FileScanner.scan(state.currentScanPath, state.currentScanResult);

        try {

            state.actionQueue.put(result);
        
        } catch (InterruptedException e) {
            
            Thread.currentThread().interrupt();
            return;
        
        }

    }

    /*
     * ============================================================
     * CANCEL SCAN
     * ============================================================
     */
    public static void cancelScan(
        AtlasState state
    ) {

        System.out.println("\nCancel scan requested...");
        System.out.println("Cancelling scan...");

        boolean cancelled = state.scanFuture.cancel(true);
        
        if (cancelled) {
            
            System.out.println("\nScan cancelled successfully!");
            String message = """

                            Scan Progress result :- 
                            Files Found: """ + state.currentScanResult.getFilesFound() + """

                            Folders Found: """ + state.currentScanResult.getFoldersFound() + """

                            Skipped: """ + state.currentScanResult.getSkippedFound() + """
                            """;            
            
            System.out.println(message);
            
            
            state.currentState = AtlasState.State.MAIN_MENU;
            state.currentScanPath = null;
            state.currentScanResult = null;
            state.scanFuture = null;

        }

        else {
            System.out.println("Scan could not be cancelled, please try again.");
        }
        

    }
    

    /*
     * ============================================================
     * PROGRESS
     * ============================================================
     */
    public static void showProgress(
        AtlasState state
    ) {

        String message;

        if (state.currentState == AtlasState.State.SCANNING) {

            message = """
                    
                    Scan Progress result :- 
                    Files Found: """ + state.currentScanResult.getFilesFound() + """

                    Folders Found: """ + state.currentScanResult.getFoldersFound() + """
                    
                    Skipped: """ + state.currentScanResult.getSkippedFound() + """
                    """;

        }

        else {

            message = "No scan progress available.";
        }

        System.out.println(message);
        
    }


}
