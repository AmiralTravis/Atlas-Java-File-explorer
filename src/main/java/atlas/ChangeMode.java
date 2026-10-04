package atlas;

public class ChangeMode {
    
    static  void mainMenuMode(AtlasState state) {

        state.currentState = AtlasState.State.MAIN_MENU;

    }

    static void indexMode(AtlasState state) {

        state.currentState = AtlasState.State.INDEX_MENU;

    }

    static void browseMode(AtlasState state) {

        state.currentState = AtlasState.State.BROWSE_MENU;

    }
    
}
