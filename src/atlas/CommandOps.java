package atlas;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import atlas.scanner.ScanUtils;


public class CommandOps {


    static String handleCommand(String command, AtlasState state, ExecutorService executor) {

        Set<String> isNonThreadOp = Set.of("show root", "show current", "parent dir", "progress", "cancel");
        Set<String> isThreadOp = Set.of("scan");
        Set<String> isMainMenuCommand = Set.of("show root", "show current", "parent dir", "scan");
        Set<String> isScanMenuCommand = Set.of("progress", "cancel");

        if (isNonThreadOp.contains(command) 
            && isMainMenuCommand.contains(command) 
            && state.currentState.equals(AtlasState.State.MAIN_MENU)
        ) {
            CommandOps.runNonThreadOp(command, state);
        }

        else if (isNonThreadOp.contains(command) 
            && isScanMenuCommand.contains(command) 
            && state.currentState.equals(AtlasState.State.SCANNING)
        ) {
            CommandOps.runNonThreadOp(command, state);
        }

        else if (command.startsWith("open ")
            && state.currentState.equals(AtlasState.State.MAIN_MENU)
        ) {

            String itemName = command.substring(5);

            DirectoryBrowser.openItem(itemName, state);
        }

        else if (isThreadOp.contains(command)
            && isMainMenuCommand.contains(command) 
            && state.currentState.equals(AtlasState.State.MAIN_MENU)
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

        if (command.equals("show root")) {
            DirectoryBrowser.showRoot();
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


    }




    static void runThreadOp(String command, AtlasState state, ExecutorService executor) {

        if (command.equals("scan")) {

            if (state.currentScanPath != null) {

                System.out.println("\nAlready scanning at: " + state.currentScanPath 
                + "\nTry again after the scan is done.");
                
                return;
            }

            state.currentState = AtlasState.State.SCANNING;

            state.scanFuture = executor.submit(() -> {
                ScanUtils.startScan(state);;
            });


                        
            
        }
    }


}
