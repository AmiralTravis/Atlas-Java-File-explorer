package atlas.usn;

import java.io.IOException;
import java.nio.file.Path;

import atlas.AtlasState;
import atlas.scanner.FileSystemNode;
import atlas.scanner.Index;
import atlas.scanner.IndexStore;

public class UpdateIndex {
    
    static void updateIndexByUsn(UsnUpdateRecords updateRecord, AtlasState state) {

        System.out.println("\n\n");
        state.atlasIndex.printTree();
        System.out.println("\n\n");

        for (UsnRecord usnUpdateRecord : updateRecord.UsnUpdateRecordsList) {

            if (usnUpdateRecord.getChangeType().equals("New file")) {
                newFileUpdation(state);
            }

            else if (usnUpdateRecord.getChangeType().equals("Deleted file")) {
                deleteFileUpdation(state);
            }

            else if (usnUpdateRecord.getChangeType().equals("Renamed file")) {
                renameFileUpdation(usnUpdateRecord, state);
            }

            else if (usnUpdateRecord.getChangeType().equals("Updated file contents")) {
                updateFileUpdation(state);
            }

            else {
                System.out.println("Issue occured while updating index. Invalid USN reason.");
                return;
            }
        }


        System.out.println("\n\n");
        state.atlasIndex.printTree();
        System.out.println("\n\n");

        try {

            state.atlasIndex.lastProcessedUsn = updateRecord.lastProcessedUsn;
    
            Path indexPath = state.indexPath;
            
            IndexStore.writeIndex(state.atlasIndex, indexPath);

        } catch (IOException e) {

            System.err.println("Failed to save Index to disk");
            e.printStackTrace();

        }


        
    }

    static void newFileUpdation(AtlasState state) {
        System.out.println("dummy new file updation logic");
    }

    static void deleteFileUpdation(AtlasState state) {
        System.out.println("dummy delete file updation logic");
    }


    static void renameFileUpdation(
        UsnRecord record,
        AtlasState state
    ) {

        Index index = state.atlasIndex;

        FileSystemNode currentNode = index.getNode(record.getFileRef());

        if (currentNode == null) {
            return;
        }

        Path actualPath = record.getFullPath();

        // 1. Handle the original USN node separately.
        String actualName = actualPath.getFileName().toString();

        if (!currentNode.getName().equals(actualName)) {
            currentNode.setName(actualName);
        }

        // 2. Move to its parent node logic.
        FileSystemNode childNode = currentNode;

        currentNode = index.getNode(currentNode.getParentId());

        // 3. Reconcile ancestors using file IDs.
        Path actualParentPath = actualPath.getParent();

        if (actualParentPath == null) {
            // break;
            return;
        }

        long actualParentId = ReadUsn.getFileId(actualParentPath);

        
        FileSystemNode newParent = index.getNode(actualParentId);

        // if (newParent == null) {
        //     System.out.println(
        //         "Actual parent not found in index. Rescan required." + 
        //         "(OR)" + 
        //         "reached end of tree in index."
        //     );
        //     return;
        // }

        FileSystemNode oldParent = currentNode;

        
        oldParent.removeChild(childNode);

        childNode.setParentId(actualParentId);
        
        newParent.addChild(childNode);

        
    }




    
    static void updateFileUpdation(AtlasState state) {
        System.out.println("dummy update file updation logic");
    }
}
