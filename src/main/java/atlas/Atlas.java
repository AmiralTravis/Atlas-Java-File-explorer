package atlas;

import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import atlas.scanner.IndexStore;
import atlas.scanner.Index;

class Atlas {

    public static void main(String[] args) {
        
        System.out.println("\nWelcome to Atlas File Search & Management Application!\n");

        // System.out.println(
        //     Path.of("").toAbsolutePath()
        // );

        Path indexFile = Path.of("atlas.index");
        
        AtlasState state = new AtlasState();

        if (Files.exists(indexFile)) {

            try {

                Index atlasIndex = IndexStore.readIndex(indexFile);
                state = new AtlasState(atlasIndex);

            } catch (IOException | ClassNotFoundException e) {

                System.err.println("Unable to load index file");
                e.printStackTrace();

                state = new AtlasState();

            }

        }



        InputThread input = new InputThread(state);

        Thread inputThread = new Thread(input);

        inputThread.start();


        ExecutorService executor = Executors.newFixedThreadPool(2);


        while (true) {


            if (state.currentState.equals(AtlasState.State.EXITING)) {
                System.out.println("Exiting application...\n");
                System.exit(0);
            }
            

            QueueItem item;

            String menu = AtlasMenu.menuRender(state);

            System.out.println(menu);

            try {

                item = state.actionQueue.take();
            
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                return;
            
            }
            
            if (item instanceof CommandItem commandItem) {
                
                String command = commandItem.getValue();
                
                String result = CommandOps.handleCommand(command, state, executor);

                if (result.equals("invalid command")) {
                    System.out.println("Invalid command, please try again.\n");
                }

            }

            else {

                RenderOutput.renderResult(item);

                // System.out.println("\n====================================\n");
                // System.out.println("AtlasIndex:");
                // System.out.println("fileList: [" );
                // Index.showFileRecords(state.atlasIndex.fileList);
                // System.out.println("]");
                // System.out.println("\n====================================\n");
                // System.out.println("folderList: " + state.atlasIndex.folderList);
                // System.out.println("\n====================================\n");

                if (state.currentState.equals(AtlasState.State.SCANNING)) {
                    
                    state.currentState = AtlasState.State.INDEX_MENU;
                    state.currentScanPath = null;
                    state.currentScanResult = null;
                    state.scanFuture = null;
                
                }

                else {
                    state.currentState = AtlasState.State.MAIN_MENU;
                }

            }
            



        }
        

    }



}