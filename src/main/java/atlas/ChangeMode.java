package atlas;

import java.nio.file.Path;
import java.util.ArrayList;

import atlas.scanner.ScanUtils;

public class ChangeMode {
    
    static  void mainMenuMode(AtlasState state) {

        state.currentState = AtlasState.AppState.MAIN_MENU;

    }

    public static void indexMode(AtlasState state) {

        state.currentState = AtlasState.AppState.INDEX_MENU;

        if (ScanUtils.indexExists(state)){
            
            state.indexState = AtlasState.IndexMenuState.WITH_INDEX;
        
        } else {

            state.indexState = AtlasState.IndexMenuState.NO_INDEX;

        }

        state.confState = AtlasState.ConfirmationMenuState.NONE;

    }

    static void browseMode(AtlasState state) {

        state.currentState = AtlasState.AppState.BROWSE_MENU;

    }

    static void updateIndexMode(AtlasState state) {

        state.indexState = AtlasState.IndexMenuState.UPDATE_INDEX;
    
    }

    static void deleteIndexMode(AtlasState state) {

        state.confState = AtlasState.ConfirmationMenuState.DELETE_INDEX;
    
    }

    static void fullRescanMode(AtlasState state) {

        state.confState = AtlasState.ConfirmationMenuState.RESCAN;
    
    }

    static void preScanMode(AtlasState state) {

        // ==========================================================================
        // this case would probably never occur, 
        // bcz 'create index' would be invalid command in SCANNING app state
        // but still keeping it as legacy code,
        //  will remove this later

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

        // ==========================================================================


        state.currentState = AtlasState.AppState.PRE_SCANNING;  

        state.tempPaths = new ArrayList<Path>();

    }

    public static void scanMode(AtlasState state) {

        state.currentState = AtlasState.AppState.SCANNING;
    
    }
    
    
}
