package atlas;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class AtlasMenu {



    static String menuRender(AtlasState state) {

        String message;

        AtlasState.AppState appState = state.currentState;
        AtlasState.IndexMenuState indexState = state.indexState;
        AtlasState.ConfirmationMenuState confState = state.confState;

        if (appState.equals(AtlasState.AppState.MAIN_MENU)) {
        
            // message = """

            //         ===============MAIN MENU===============

            //         Current path: """+ state.currentPath +"""


            //         Menu Options: 
                    
            //         show root
            //         show current
            //         open {name}
            //         parent dir
            //         scan
            //         exit

            //         Enter command:
            //         """;

            message = """
                    ===============MAIN MENU===============

                    Select mode:

                    browse
                    index
                    
                    Enter command:
                    """;
            
            state.currentCommands = new ArrayList<>(List.of(
                "browse",
                "index"
            ));

            return message;
        }

        else if (appState.equals(AtlasState.AppState.BROWSE_MENU)) {

            message = """
                    ==============BROWSE MENU==============
                    
                    Current path: """+ state.currentPath +"""


                    Menu Options:

                    show current
                    open {name}
                    parent dir
                    main menu

                    Enter command: 
                    """;

            state.currentCommands = new ArrayList<>(List.of(
                "show current",
                "parent dir",
                "main menu"
            ));

            return message;

        }

        else if (appState.equals(AtlasState.AppState.INDEX_MENU)) {

            if (confState.equals(AtlasState.ConfirmationMenuState.NONE)) {

                if (indexState.equals(AtlasState.IndexMenuState.WITH_INDEX)) {
                    message = """
                            ==============INDEX MENU===============

                            update index
                            about index
                            delete index
                            main menu

                            Enter command:
                            """;

                    state.currentCommands = new ArrayList<>(List.of(
                        "update index",
                        "about index",
                        "delete index",
                        "main menu"
                    ));
        
                    return message;
                }

                else if (indexState.equals(AtlasState.IndexMenuState.UPDATE_INDEX)) {
                    message = """
                            ==============UPDATE INDEX MENU===============

                            full rescan
                            use usn
                            index menu
                            main menu

                            Enter command:
                            """;

                    state.currentCommands = new ArrayList<>(List.of(
                        "full rescan",
                        "use usn",
                        "index menu",
                        "main menu"
                    ));

                    return message;
                }

                else if (indexState.equals(AtlasState.IndexMenuState.NO_INDEX)) {
                    message = """
                            ==============INDEX MENU===============

                            create index
                            about index
                            main menu

                            Enter command:
                            """;

                    state.currentCommands = new ArrayList<>(List.of(
                        "create index",
                        "about index",
                        "main menu"
                    ));

                    return message;
                }

                else { 

                    message = "invalid index state";

                    state.currentCommands = new ArrayList<>();

                    return message;
                
                }

            }
            
            else { //confState != NONE

                if (
                    confState.equals(AtlasState.ConfirmationMenuState.DELETE_INDEX) &&
                    indexState.equals(AtlasState.IndexMenuState.WITH_INDEX)
                ) {

                    message = """
                            ==============DELETE INDEX MENU===============
                            Are you sure you want to delete index? 
                            You will lose x bytes worth of current indexed data. 
                            This action can not be undone. 

                            proceed
                            cancel

                            Enter command:
                            """;

                    state.currentCommands = new ArrayList<>(List.of(
                        "proceed",
                        "cancel"
                    ));

                    return message;
                }

                else if (
                    confState.equals(AtlasState.ConfirmationMenuState.RESCAN) &&
                    indexState.equals(AtlasState.IndexMenuState.UPDATE_INDEX)
                ) {

                    message = """
                            ==============RESCAN INDEX MENU===============
                            Are you sure you want a full rescan? 
                            This will delete the current index and build index from scratch. 
                            You will lose x bytes worth of current index data. 
                            This action can not be undone.

                            proceed
                            cancel

                            Enter command:
                            """;

                    state.currentCommands = new ArrayList<>(List.of(
                        "proceed",
                        "cancel"
                    ));

                    return message;

                }


                message = "invalid index state";

                state.currentCommands = new ArrayList<>();

                return message;
            }

        }

        else if (appState.equals(AtlasState.AppState.PRE_SCANNING)) {
            

            List<Path> dirs = state.tempPaths;

            StringBuilder dirList = new StringBuilder();

            for (Path dir : dirs) {
                dirList.append(dir).append("\n");
            }

            message = """

                    ===========PRE-SCANNER MENU============

                    Type your directory full paths 
                    that you want the index to consist of
                    in the following manner, for example:
                    
                    C:\\Users\\asbia\\Downloads

                    then press enter. 
                    Or type 'done' and press enter once done.
                    Or type 'close' and press enter to discard and exit the scanning process.

                    Directories taken:
                    """ + dirList + """


                    Enter command:
                    """;

            state.currentCommands = new ArrayList<>(List.of(
                "done",
                "close"
            ));

            return message;
        
        }

        else if (appState.equals(AtlasState.AppState.SCANNING)) {
            
            message = """
                    ---------------------------------------
                    Scanning...
                    ---------------------------------------

                    ==============SCANNER MENU=============

                    Menu Options:
                    
                    progress
                    cancel

                    Enter command:
                    """;

            state.currentCommands = new ArrayList<>(List.of(
                "progress",
                "cancel"
            ));

            return message;
        
        }

        else {

            message = "invalid state";
            
            state.currentCommands = new ArrayList<>();

            return message;
        }
    }



    
}