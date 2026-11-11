package atlas.usn;

import java.io.IOException;
import java.nio.file.Path;

import atlas.AtlasState;
import atlas.scanner.IndexStore;

public class UpdateIndex {
    
    static void updateIndexByUsn(UsnUpdateRecords updateRecord, AtlasState state) {


        for (UsnRecord usnUpdateRecord : updateRecord.UsnUpdateRecordsList) {

            if (usnUpdateRecord.getChangeType().equals("New file")) {
                newFileUpdation();
            }

            else if (usnUpdateRecord.getChangeType().equals("Deleted file")) {
                deleteFileUpdation();
            }

            else if (usnUpdateRecord.getChangeType().equals("Renamed file")) {
                renameFileUpdation();
            }

            else if (usnUpdateRecord.getChangeType().equals("Updated file contents")) {
                updateFileUpdation();
            }

            else {
                System.out.println("Issue occured while updating index. Invalid USN reason.");
                return;
            }
        }

        try {

            state.atlasIndex.lastProcessedUsn = updateRecord.lastProcessedUsn;
    
            Path indexPath = state.indexPath;
            
            IndexStore.writeIndex(state.atlasIndex, indexPath);

        } catch (IOException e) {

            System.err.println("Failed to save Index to disk");
            e.printStackTrace();

        }


        
    }

    static void newFileUpdation() {
        System.out.println("dummy new file updation logic");
    }

    static void deleteFileUpdation() {
        System.out.println("dummy delete file updation logic");
    }

    static void renameFileUpdation() {
        System.out.println("dummy rename file updation logic");
    }

    static void updateFileUpdation() {
        System.out.println("dummy update file updation logic");
    }
}
