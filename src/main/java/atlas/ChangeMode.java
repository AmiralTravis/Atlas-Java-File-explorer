package atlas;

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

    
    
}
