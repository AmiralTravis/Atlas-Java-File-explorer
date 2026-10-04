package atlas;


class AtlasMenu {



    static String menuRender(AtlasState state) {

        String message;

        AtlasState.State phase = state.currentState;

        if (phase.equals(AtlasState.State.MAIN_MENU)) {
        
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
            
            return message;
        }

        else if (phase.equals(AtlasState.State.BROWSE_MENU)) {

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

            return message;

        }

        else if (phase.equals(AtlasState.State.INDEX_MENU)) {

            message = """
                    ==============INDEX MENU===============

                    update index
                    about index
                    main menu

                    Enter command:
                    """;

            return message;

        }

        else if (phase.equals(AtlasState.State.SCANNING)) {
            
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

            return message;
        
        }

        else {

            message = "invalid state";
            
            return message;
        }
    }



    
}