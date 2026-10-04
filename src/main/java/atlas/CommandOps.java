package atlas;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import atlas.scanner.ScanUtils;


public class CommandOps {


    static String handleCommand(String command, AtlasState state, ExecutorService executor) {

        Set<String> isNonThreadOp = Set.of(
            "show current", "parent dir", "progress", "cancel", "browse", "index",
            "about index", "main menu"
        );
        Set<String> isThreadOp = Set.of("update index");
        // Set<String> isMainMenuCommand = Set.of("show current", "parent dir", "scan");
        Set<String> isMainMenuCommand = Set.of("browse", "index");
        Set<String> isIndexMenuCommand = Set.of("update index", "about index", "main menu");
        Set<String> isBrowseMenuCommand = Set.of("show current", "parent dir", "main menu");
        Set<String> isScanMenuCommand = Set.of("progress", "cancel");

        if (isNonThreadOp.contains(command) 
            && isMainMenuCommand.contains(command) 
            && state.currentState.equals(AtlasState.State.MAIN_MENU)
        ) {
            CommandOps.runNonThreadOp(command, state);
        }

        else if (isNonThreadOp.contains(command) 
            && isIndexMenuCommand.contains(command) 
            && state.currentState.equals(AtlasState.State.INDEX_MENU)
        ) {
            CommandOps.runNonThreadOp(command, state);
        }

        else if (isNonThreadOp.contains(command) 
            && isBrowseMenuCommand.contains(command) 
            && state.currentState.equals(AtlasState.State.BROWSE_MENU)
        ) {
            CommandOps.runNonThreadOp(command, state);
        }

        else if (command.startsWith("open ")
            && state.currentState.equals(AtlasState.State.BROWSE_MENU)
        ) {

            String itemName = command.substring(5);

            DirectoryBrowser.openItem(itemName, state);
        }

        else if (isNonThreadOp.contains(command) 
            && isScanMenuCommand.contains(command) 
            && state.currentState.equals(AtlasState.State.SCANNING)
        ) {
            CommandOps.runNonThreadOp(command, state);
        }        

        else if (isThreadOp.contains(command)
            && isIndexMenuCommand.contains(command) 
            && state.currentState.equals(AtlasState.State.INDEX_MENU)
        ) {
            CommandOps.runThreadOp(command, state, executor);
        }

        else if (command.equals("exit")) {
            state.currentState = AtlasState.State.EXITING;
        }

        else {
            return "invalid command";
        }

        return "";

    }




    
    static void runNonThreadOp(String command, AtlasState state) {

        if (command.equals("browse")) {
            ChangeMode.browseMode(state);
        }

        else if (command.equals("index")) {
            ChangeMode.indexMode(state);
        }



        else if (command.equals("show current")) {
            DirectoryBrowser.showCurrent(state);
        }

        else if (command.equals("parent dir")) {
            DirectoryBrowser.parentDir(state);
        }



        else if (command.equals("progress")) {
            ScanUtils.showProgress(state);
        }

        else if (command.equals("cancel")) {
            ScanUtils.cancelScan(state);
        }



        else if (command.equals("about index")) {
            ScanUtils.aboutIndex(state);
        }

        else if (command.equals("main menu")) {
            ChangeMode.mainMenuMode(state);
        }


    }




    static void runThreadOp(String command, AtlasState state, ExecutorService executor) {

        if (command.equals("update index")) {

            if (state.currentState.equals(AtlasState.State.SCANNING)) {
                
                if (state.currentScanPath != null) {

                    System.out.println("Already scanning at: " + state.currentScanPath 
                    + "\nTry again after the scan is done.\n");
                    
                }

                else {
                    System.out.println("Already scanning something.");
                    System.out.println("Try again after the scan is done.\n");
                }

                return;

            }

            state.currentState = AtlasState.State.SCANNING;

            state.scanFuture = executor.submit(() -> {
                ScanUtils.startScan(state);;
            });


                        
            
        }
    }


}
