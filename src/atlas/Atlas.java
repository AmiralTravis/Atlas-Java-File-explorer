package atlas;

import java.util.Scanner;

import atlas.scanner.FileScanner;

public class Atlas {

    public static void main(String[] args) {

        System.err.println(
            "Welcome to Atlas File Explorer:\n"
        );

        Scanner scanner = new Scanner(System.in);

        AtlasState state =
            new AtlasState();

        FileScanner fileScanner =
            new FileScanner();


        while (true) {

            /*
             * If a scan has finished, display its result
             * before showing the next menu.
             */
            // if (
            //     !state.scanRunning.get()
            //     && state.scanResult != null
            // ) {

            //     if (state.cancelScanRequested.get()) {

            //         System.out.println(
            //             "Scan cancelled."
            //         );

            //     } else {

            //         System.out.println(
            //             "Scan complete."
            //         );
            //     }

            //     System.out.println(
            //         "Files Found: " +
            //         state.scanResult.getFilesFound()
            //     );

            //     System.out.println(
            //         "Folders Found: " +
            //         state.scanResult.getFoldersFound()
            //     );

            //     System.out.println();

            //     state.scanResult = null;
            // }


            /*
             * Display the appropriate menu.
             */
            AtlasMenu.show(state);


            /*
             * Wait for user input.
             */
            String input = scanner.nextLine();


            /*
             * Let AtlasMenu handle the command.
             */
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

                scanner.close();

                return;
            }


            /*
             * Print command result if there is one.
             */
            if (!result.isEmpty()) {

                System.out.println(result);
            }
        }
    }
}