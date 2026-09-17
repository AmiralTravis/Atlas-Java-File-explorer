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
                "\nUnable to access root directory."
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
                "\nUnable to access: " +
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
    static String openItem(
        String itemName,
        AtlasState state
    ) {

        File item =
            state.currentPath
                .resolve(itemName)
                .toFile();


        if (!item.exists()) {

            System.out.println("\nThis item doesn't exist in this directory.");

            return "";
        }


        if (item.isFile()) {

            try {

                Desktop.getDesktop().open(item);
                
                return "";
            }

            catch (Exception e) {
                System.err.println("Error opening file: " + e);
            }

        }


        /*
         * Opening a directory only changes our
         * current path.
         */
        if (item.isDirectory()) {

            state.currentPath =
                item.toPath();

            return "";
        }

        return "";
    }



    /*
     * ============================================================
     * PARENT DIRECTORY
     * ============================================================
     */
    static String parentDir(
        AtlasState state
    ) {

        if (
            state.currentPath.getParent() != null
        ) {

            state.currentPath =
                state.currentPath.getParent();

            return "";
        }

        else {

            System.out.println("\nAlready at root dir!");
            return "";

        }
    }






}
