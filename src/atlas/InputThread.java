package atlas;

import java.util.Scanner;

public class InputThread implements Runnable {

    private final AtlasState state;
    private final Scanner scanner;


    public InputThread(
        AtlasState state,
        Scanner scanner
    ) {

        this.state = state;
        this.scanner = scanner;
    }


    @Override
    public void run() {

        try {

            while (true) {

                /*
                 * This is the ONLY thread that waits
                 * for keyboard input.
                 */
                if (!scanner.hasNextLine()) {

                    return;
                }


                String input =
                    scanner.nextLine();


                /*
                 * Don't process the command here.
                 *
                 * Just hand it to the main thread.
                 */
                state.commandQueue.offer(input);
            }

        } catch (Exception e) {

            /*
             * Input stream closed during shutdown —
             * safe to ignore.
             */
        }
    }
}