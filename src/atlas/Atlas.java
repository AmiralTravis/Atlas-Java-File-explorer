package atlas;

import java.util.Scanner;
import java.awt.Desktop;
import java.nio.file.Path;
import java.nio.file.Paths;

import atlas.scanner.FileScanner;
import atlas.scanner.ScanResult;

class Atlas {

    public static void main(String[] args) {
        
        System.err.println("Welcome to Atlas File Explorer:\n");

        Scanner scanner = new Scanner(System.in);

        AtlasState state = new AtlasState(Paths.get("\\"));
        FileScanner fileScanner = new FileScanner();

        while (true) {

            System.out.println("Current Path: " + state.currentPath + "\n");

            System.out.println("Menu:");

            System.out.println(
                "show root\n" + 
                "show current\n" +
                "open {name}\n" +
                "parent dir\n" +
                "scan\n" +
                "exit\n"
            );

            String input = scanner.nextLine();

            String Menu_result = Menu_runner(input, state, fileScanner);

            if(Menu_result.equals("exit")) {
                scanner.close();
                return;
            }

            System.out.println(Menu_result);
        }

        
        
    }

    private static String Menu_runner(
        String a, 
        AtlasState state, 
        FileScanner fileScanner
    ) {
        
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

            Path itemPath = state.currentPath.resolve(itemName);

            Open_item(itemPath, state);
            
            return "\n";
        }

        else if (a.equals("parent dir")) {

            Parent_dir(state);

            return "\n";

        }

        else if (a.equals("scan")) {

            ScanResult result = fileScanner.scan(state.currentPath);

            System.out.println("\nScan Complete.");
            System.out.println("Files Found: " + result.getFilesFound());
            System.out.println("Folders Found: " + result.getFoldersFound());
            System.out.println("Skipped: " + result.getSkippedFound());

            return "\n";

        }

        else if (a.equals("exit")) {

            return "exit";
        }

        else {return "Invalid input\n";}
    }

    private static void Show_root() {

        Path directory = Paths.get("\\");

        try (var items = java.nio.file.Files.list(directory)) {

            items.forEach(item -> {

                if (java.nio.file.Files.isRegularFile(item)) {
                    System.out.println(item.getFileName());
                }

                else if (java.nio.file.Files.isDirectory(item)) {
                    System.out.println("\\" + item.getFileName());
                }

            });

        } catch (Exception e) {

            System.err.println("Could not read root directory: " + e.getMessage());

        }
    }

    private static void Show_current(AtlasState state) {

        Path directory = state.currentPath;

        try (var items = java.nio.file.Files.list(directory)) {

            items.forEach(item -> {

                if (java.nio.file.Files.isRegularFile(item)) {
                    System.out.println(item.getFileName());
                }

                else if (java.nio.file.Files.isDirectory(item)) {
                    System.out.println("\\" + item.getFileName());
                }

            });

        } catch (Exception e) {

            System.err.println("Could not read directory: " + e.getMessage());

        }
    }

    private static void Open_item(Path itemPath, AtlasState state) {

        if (java.nio.file.Files.exists(itemPath)) {

            if (java.nio.file.Files.isRegularFile(itemPath)) {

                try {

                    Desktop.getDesktop().open(itemPath.toFile());

                } catch (Exception e) {

                    System.err.println(
                        "Error occurred while opening the file "
                        + itemPath + ": " + e
                    );

                }

            }

            else if (java.nio.file.Files.isDirectory(itemPath)) {

                state.currentPath = itemPath;

                Show_current(state);

            }

        }

        else {

            System.err.println("This item doesn't exist in this directory.");

        }
    }




    private static Path Parent_dir(AtlasState state) {

        Path parent = state.currentPath.getParent();
 
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


