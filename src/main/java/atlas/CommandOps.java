package atlas;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import atlas.scanner.ScanUtils;


public class CommandOps {


    static Set<String> isNonThreadOp = Set.of(
        "show current", "parent dir", "progress", "cancel", "browse", "index",
        "about index", "main menu", 
        "update index", "delete index", "index menu", "full rescan", "use usn"
    );
    static Set<String> isThreadOp = Set.of("create index");

    static String handleCommand(String command, AtlasState state, ExecutorService executor) {

        if (command.equals("exit")) {
            state.currentState = AtlasState.AppState.EXITING;

            return "";
        }

        if (
            command.startsWith("open ")
            && state.currentState.equals(AtlasState.AppState.BROWSE_MENU)
        ) {

            String itemName = command.substring(5);

            DirectoryBrowser.openItem(itemName, state);

            return "";
        }

        if (
            command.equals("proceed") | command.equals("cancel")
            && !state.confState.equals(AtlasState.ConfirmationMenuState.NONE)
        ) {
            handleReconfirmation(command, state);
            return "";
        }

        if (!state.currentCommands.contains(command)) {
            return "invalid command";
        }


        if (isThreadOp.contains(command)) {
            runThreadOp(command, state, executor);
        }

        else if (isNonThreadOp.contains(command)) {
            runNonThreadOp(command, state);
        }
     

        else {
            return "invalid command";
        }

        return "";

    }


    static void handleReconfirmation(String command, AtlasState state) {
        if (state.confState.equals(AtlasState.ConfirmationMenuState.DELETE_INDEX)) {
            if (command.equals("proceed")) {
                ScanUtils.runDeleteIndex(state);
            }

            else {
                state.confState = AtlasState.ConfirmationMenuState.NONE;
                state.indexState = AtlasState.IndexMenuState.WITH_INDEX;
            }
            
        }

        else if (state.confState.equals(AtlasState.ConfirmationMenuState.RESCAN)) {
            if (command.equals("proceed")) {
                ScanUtils.runReScan(state);
            }

            else {
                state.confState = AtlasState.ConfirmationMenuState.NONE;
                state.indexState = AtlasState.IndexMenuState.UPDATE_INDEX;
            }
        }
    }


    
    static void runNonThreadOp(String command, AtlasState state) {

        if (command.equals("browse")) {
            ChangeMode.browseMode(state);
        }

        else if (command.equals("index") | command.equals("index menu")) {
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

        else if (command.equals("update index")) {
            ChangeMode.updateIndexMode(state);
        }

        else if (command.equals("delete index")) {
            ChangeMode.deleteIndexMode(state);
        }


        else if (command.equals("full rescan")) {
            ChangeMode.fullRescanMode(state);
        }

        else if (command.equals("use usn")) {
            ScanUtils.updateIndexByUsnJournal(state);
        }

        

    }




    static void runThreadOp(String command, AtlasState state, ExecutorService executor) {

        if (command.equals("create index")) {

            if (state.currentState.equals(AtlasState.AppState.SCANNING)) {
                
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

            state.currentState = AtlasState.AppState.SCANNING;

            state.scanFuture = executor.submit(() -> {
                ScanUtils.startScan(state);;
            });


                        
            
        }
    }


}
