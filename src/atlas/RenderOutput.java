package atlas;

import atlas.scanner.ScanResult;

public class RenderOutput {
    
    static void renderResult(QueueItem item) {
        
        if (item instanceof ScanResult scanResult) {

            System.out.println("\nScanning Finished!");
            System.out.println("\nScan result :-");
            System.out.println("Files found: " + scanResult.getFilesFound());
            System.out.println("Folders found: " + scanResult.getFoldersFound());
            System.out.println("Skipped: " + scanResult.getSkippedFound() + "\n");

        }
        
    }


}
