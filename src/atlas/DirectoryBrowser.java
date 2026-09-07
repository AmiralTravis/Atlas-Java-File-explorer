package atlas;

import java.io.File;
import java.awt.Desktop;

public class DirectoryBrowser {
    

    
    /*
     * ============================================================
     * ROOT
     * ============================================================
     */
    static void showRoot() {

        File directory =
            new File("\\");

        File[] items =
            directory.listFiles();


        if (items == null) {

            System.out.println(
                "Unable to access root directory."
            );

            return;
        }


        for (File item : items) {

            if (item.isFile()) {

                System.out.println(
                    item.getName()
                );
            }

            else if (item.isDirectory()) {

                System.out.println(
                    "\\" +
                    item.getName()
                );
            }
        }
    }



    /*
     * ============================================================
     * CURRENT DIRECTORY
     * ============================================================
     */
    static void showCurrent(
        AtlasState state
    ) {

        File directory =
            state.currentPath.toFile();

        File[] items =
            directory.listFiles();


        if (items == null) {

            System.out.println(
                "Unable to access: " +
                state.currentPath
            );

            return;
        }


        for (File item : items) {

            if (item.isFile()) {

                System.out.println(
                    item.getName()
                );
            }

            else if (item.isDirectory()) {

                System.out.println(
                    "\\" +
                    item.getName()
                );
            }
        }
    }



    /*
     * ============================================================
     * OPEN ITEM
     * ============================================================
     */
    static void openItem(
        String itemName,
        AtlasState state
    ) {

        File item =
            state.currentPath
                .resolve(itemName)
                .toFile();


        if (!item.exists()) {

            state.lastMessage =
                "This item doesn't exist in this directory.";

            state.uiNeedsRender = true;

            return;
        }


        /*
         * Opening a file means opening it with the
         * operating system.
         */
        if (item.isFile()) {

            try {

                Desktop.getDesktop().open(item);

            }

            catch (Exception e) {

                state.lastMessage =
                    "Error opening file " +
                    item +
                    ": " +
                    e.getMessage();

                state.uiNeedsRender = true;
            }

            return;
        }


        /*
         * Opening a directory only changes our
         * current path.
         */
        if (item.isDirectory()) {

            state.currentPath =
                item.toPath();

            state.uiNeedsRender = true;
        }
    }



    /*
     * ============================================================
     * PARENT DIRECTORY
     * ============================================================
     */
    static void parentDir(
        AtlasState state
    ) {

        if (
            state.currentPath.getParent() != null
        ) {

            state.currentPath =
                state.currentPath.getParent();

            state.uiNeedsRender = true;
        }

        else {

            state.lastMessage =
                "Already at root dir!";

            state.uiNeedsRender = true;
        }
    }






}
