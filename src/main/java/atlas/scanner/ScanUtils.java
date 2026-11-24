package atlas.scanner;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import atlas.AtlasState;
import atlas.ChangeMode;
import atlas.usn.AutoUsnUpdater;
import atlas.usn.ReadUsn;

public class ScanUtils {
    

    /*
     * ============================================================
     * START SCAN
     * ============================================================
     */
    public static void startScan(
        AtlasState state
    ) {

        state.currentScanResult = new ScanResult();

        // build into a local index and publish it only when complete,
        // so the auto updater and search never see a half-built index
        Index newIndex = new Index();

        newIndex.lastProcessedUsn = ReadUsn.getLastRecordedUsn();

        ScanResult result = FileScanner.scan(
            state.tempPaths,
            state.currentScanResult,
            newIndex,
            state
        );

        // scan was cancelled: discard the partial index
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        newIndex.filesIndexed = result.getFilesFound();
        newIndex.foldersIndexed = result.getFoldersFound();
        newIndex.skippedItems = result.getSkippedFound();
        
        newIndex.createdAt = LocalDateTime.now();
        newIndex.modifiedAt = LocalDateTime.now();

        synchronized (state.indexLock) {

            state.atlasIndex = newIndex;

            try {

                IndexStore.writeIndex(newIndex, state.indexPath);

            } catch (IOException e) {

                System.err.println("Failed to save Index to disk");
                e.printStackTrace();

            }
        }

        // wakes the updater; it replays anything that changed during the scan
        AutoUsnUpdater.notifyIndexReady();

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
            
            
            state.currentState = AtlasState.AppState.MAIN_MENU;
            state.currentScanPath = null;
            state.currentScanResult = null;
            state.scanFuture = null;
            state.atlasIndex = null; // drop the half-built index so search can't use it

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

        if (state.currentState == AtlasState.AppState.SCANNING) {

            Path current = state.currentScanPath;
            System.out.println("\nCurrently scanning: " + (current == null ? "starting..." : current));

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

            System.out.println("No Index exists. Please build a new index to view about index.\n");
            return;
        
        }

        System.out.println("About Index: ");
        System.out.println("\nDirectories consisting: ");
        
        ArrayList<Path> dirs = state.atlasIndex.getDirectoryList(state.atlasIndex);

        for (Path dir : dirs) {
            System.out.println(dir);
        }

        System.out.println("\nIndexed files: " + state.atlasIndex.filesIndexed);
        System.out.println("Indexed folders: " + state.atlasIndex.foldersIndexed);
        System.out.println("Skipped items: " + state.atlasIndex.skippedItems);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a, d MMMM yyyy");
        System.out.println("Created on: " + state.atlasIndex.createdAt.format(formatter));
        System.out.println("Last updated: " + state.atlasIndex.modifiedAt.format(formatter) + "\n");
     
        
        // System.out.println("\n\n");
        // state.atlasIndex.printTree();
        // System.out.println("\n\n");

        
    }


    public static boolean indexExists(AtlasState state) {

        return Files.exists(state.indexPath);
    
    }




    // NON SCANNING HELPER, INDEX RELATED UTILS 
    // -- SHALL MAKE A NEW FOLDER OR FILE IF THIS SECTION GROWS IN FUTURE


    public static void runDeleteIndex(AtlasState state) {
        synchronized (state.indexLock) {
            deleteIndexLocked(state);
        }
    }

    private static void deleteIndexLocked(AtlasState state) {

        try {

            Files.deleteIfExists(state.indexPath);

            state.atlasIndex = null;
            state.currentFilters.directories.clear(); // these ids belonged to the deleted index

            System.out.println("Index deleted successfully.\n");

        } catch (IOException e) {

            System.out.println("Could not delete index: " + e.getMessage() + "\n");

        }

        // resets confState and picks NO_INDEX / WITH_INDEX from whether the file exists
        ChangeMode.indexMode(state);
    }

    public static void runReScan(AtlasState state, ExecutorService executor) {
        synchronized (state.indexLock) {
            reScanLocked(state, executor);
        }
    }

    private static void reScanLocked(AtlasState state, ExecutorService executor) {

        // 1. Remember which directories the old index covered
        Index oldIndex = state.atlasIndex;

        if (oldIndex == null) {
            try {
                oldIndex = IndexStore.readIndex(state.indexPath);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Could not read the existing index: " + e.getMessage() + "\n");
                ChangeMode.indexMode(state);
                return;
            }
        }

        ArrayList<Path> dirs = oldIndex.getDirectoryList(oldIndex);

        if (dirs.isEmpty()) {
            System.out.println("The existing index has no directories to rescan.\n");
            ChangeMode.indexMode(state);
            return;
        }

        // 2. Delete the old index
        try {
            Files.deleteIfExists(state.indexPath);
        } catch (IOException e) {
            System.out.println("Could not delete old index: " + e.getMessage() + "\n");
            ChangeMode.indexMode(state);
            return;
        }

        state.atlasIndex = null;
        state.currentFilters.directories.clear();

        // 3. Run the normal scan on the same directories
        state.tempPaths = dirs;
        state.confState = AtlasState.ConfirmationMenuState.NONE;

        System.out.println("Old index deleted. Rescanning " + dirs.size() + " director"
            + (dirs.size() == 1 ? "y" : "ies") + "...\n");

        ChangeMode.scanMode(state);

        state.scanFuture = executor.submit(() -> {
            try {
                startScan(state);
            } catch (Throwable t) {
                System.err.println("\nScan crashed at: " + state.currentScanPath);
                t.printStackTrace();
                state.atlasIndex = null;
                state.currentState = AtlasState.AppState.MAIN_MENU;
            }
        });
    }



}
