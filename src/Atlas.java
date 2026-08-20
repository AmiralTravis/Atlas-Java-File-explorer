import java.util.Scanner;
import java.awt.Desktop;
import java.io.File;

class Atlas {

    public static void main(String[] args) {
        
        System.err.println("Welcome to Atlas File Explorer:\n");

        Scanner scanner = new Scanner(System.in);

        AtlasState state = new AtlasState();

        while (true) {

            System.out.println("Current Path: " + state.currentPath + "\n");

            System.out.println("Menu:");

            System.out.println("show root\nshow current\nopen {name}\nparent dir\nexit\n");

            String input = scanner.nextLine();

            String Menu_result = Menu_runner(input, state);

            if(Menu_result == "exit") {
                scanner.close();
                return;
            }

            System.out.println(Menu_result);
        }

        
        
    }

    private static String Menu_runner(String a, AtlasState state) {
        
        if (a.equals("show root")) {
            Show_root();
            return "\n";
        }

        else if (a.equals("show current")) {

            Show_current(state);
            return "\n";

        }

        else if (a.startsWith("open ")) {

            String itemName = a.substring(5);

            if (state.currentPath.getPath().equals("\\")) {

                File itemTypeFile = new File(state.currentPath + itemName);

                Open_item(itemTypeFile, state);

            }

            else {

                File itemTypeFile = new File(state.currentPath + "\\" + itemName);

                Open_item(itemTypeFile, state);

            }
            
            return "\n";
        }

        else if (a.equals("parent dir")) {

            Parent_dir(state);

            return "\n";

        }

        else if (a.equals("exit")) {

            return "exit";
        }

        else {return "Invalid input\n";}
    }

    private static void Show_root() {

        File directory = new File("\\");

        File[] items = directory.listFiles();

        for (File item : items) {
            
            if (item.isFile()) {

                System.out.println(item.getName());
            
            }

            if (item.isDirectory()) {

                System.out.println("\\"+item.getName());
            
            }
            
        }
        
    }

    private static void Show_current(AtlasState state) {

        File directory = state.currentPath;

        File[] items = directory.listFiles();

        for (File item : items) {
            
            if (item.isFile()) {

                System.out.println(item.getName());
            
            }

            if (item.isDirectory()) {

                System.out.println("\\"+item.getName());
            
            }
            
        }

    }

    private static void Open_item(File itemTypeFile, AtlasState state) {

        // System.out.println(itemTypeFile);


        if (itemTypeFile.exists()) {

            // System.out.println(itemTypeFile);

            if (itemTypeFile.isFile()) {

                try {

                    Desktop.getDesktop().open(itemTypeFile);
                    
                } catch (Exception e) {

                    System.err.println("Error occured during opening the File " + itemTypeFile + ": " + e);

                }

            }

            else if (itemTypeFile.isDirectory()) {

                state.currentPath = itemTypeFile;

                File[] items = itemTypeFile.listFiles();

                for (File item : items) {

                    if (item.isFile()) {

                        System.out.println(item.getName());
                    
                    }

                    if (item.isDirectory()) {

                        System.out.println("\\"+item.getName());
                    
                    }

                }

            }

        }


        else {

            System.err.println("This item doesnt exist in this directory.");

        }

        
    }

    private static File Parent_dir(AtlasState state) {

        File parent = state.currentPath.getParentFile();
 
        if ( parent != null) {

            state.currentPath = parent;

            return state.currentPath;
        
        }

        else {

            System.out.println("Already at root dir!");

            return state.currentPath;

        }
        

    }

}


class AtlasState {

    File currentPath = new File("\\");

}