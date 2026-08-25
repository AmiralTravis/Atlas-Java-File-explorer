package atlas;

import java.util.Scanner;

import atlas.scanner.FileScanner;

public class Atlas {

    public static void main(String[] args) {

        System.out.println(
            "Welcome to Atlas File Explorer:\n"
        );

        Scanner scanner = new Scanner(System.in);

        AtlasState state =
            new AtlasState();

        FileScanner fileScanner =
            new FileScanner();


        /*
         * Start dedicated input thread.
         *
         * Its only job is to wait for keyboard input
         * and place commands into the command queue.
         */
        Thread inputThread =
            new Thread(
                new InputThread(state, scanner),
                "Atlas-Input"
            );

        inputThread.setDaemon(true);
        inputThread.start();


        /*
         * Main application loop.
         *
         * The main thread NEVER waits for keyboard input.
         */
        while (true) {


            /*
             * Process any command that has arrived.
             *
             * poll() returns immediately if there is
             * currently no command.
             */
            String input =
                state.commandQueue.poll();


            if (input != null) {

                String result =
                    AtlasMenu.handleCommand(
                        input,
                        state,
                        fileScanner
                    );


                /*
                 * Exit application.
                 */
                if (result.equals("exit")) {

                    System.exit(0);
                }


                /*
                 * Store command output for the main
                 * thread to render.
                 */
                if (!result.isEmpty()) {

                    state.lastMessage = result;

                    state.uiNeedsRender = true;
                }
            }


            /*
             * Check for automatic state changes.
             *
             * For example:
             *
             * Scan thread finishes
             *        ↓
             * scanRunning = false
             *        ↓
             * main thread notices here
             */
            AtlasMenu.update(state);


            /*
             * Render UI if something changed.
             */
            AtlasMenu.renderIfNeeded(state);


            /*
             * Prevent the main loop from consuming
             * an entire CPU core while idle.
             */
            try {

                Thread.sleep(50);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                return;
            }
        }
    }
}