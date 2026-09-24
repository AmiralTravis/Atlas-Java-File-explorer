package atlas.scanner;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        state.atlasIndex = new Index();
        
        ScanResult result = FileScanner.scan(
            state.currentScanPath, 
            state.currentScanResult,
            state.atlasIndex 
        );

        state.atlasIndex.filesIndexed = result.getFilesFound();
        state.atlasIndex.foldersIndexed = result.getFoldersFound();
        state.atlasIndex.skippedItems = result.getSkippedFound();
        
        LocalDateTime modifiedTime = LocalDateTime.now();
        state.atlasIndex.modifiedAt = modifiedTime;

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

        System.out.println("Cancel scan requested...");
        System.out.println("Cancelling scan...\n");

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
            System.out.println("Scan could not be cancelled, please try again.\n");
        }
        

    }
    

    /*
     * ============================================================
     * PROGRESS SCAN
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


    /*
     * ============================================================
     * UTIL METHODS
     * ============================================================
     */
    public static void aboutIndex(AtlasState state) {

        if (state.atlasIndex == null) {

            System.out.println("No Index exists. Please build an index to view about index.\n");
            return;
        
        }

        System.out.println("About Index: ");
        System.out.println("\nIndexed files: " + state.atlasIndex.filesIndexed);
        System.out.println("Indexed folders: " + state.atlasIndex.foldersIndexed);
        System.out.println("Skipped items: " + state.atlasIndex.skippedItems);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a, d MMMM yyyy");
        System.out.println("Last updated: " + state.atlasIndex.modifiedAt.format(formatter) + "\n");
        
    }
}
