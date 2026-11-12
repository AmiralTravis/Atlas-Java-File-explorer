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
                // renameFileUpdation(state);
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

    static void renameFileUpdation(AtlasState state) {
        System.out.println("dummy rename file updation logic");
    }
    



    // static void renameFileUpdation(
    //     UsnRecord record,
    //     AtlasState state
    // ) {

    //     Index index = state.atlasIndex;

    //     index.printTree();

    //     FileSystemNode currentNode = index.getNode(record.getFileRef());

    //     if (currentNode == null) {
    //         return;
    //     }

    //     Path actualPath = record.getFullPath(); // Illustrative

    //     // 1. Handle the file node separately.
    //     String actualName = actualPath.getFileName().toString();

    //     if (!currentNode.getName().equals(actualName)) {
    //         currentNode.setName(actualName);
    //     }

    //     // 2. Move to the parent.
    //     actualPath = actualPath.getParent();

    //     if (actualPath == null) {
    //         return;
    //     }

    //     currentNode = index.getNode(currentNode.getParentId());

    //     // 3. Reconcile ancestors.
    //     while (currentNode != null) {

    //         actualName = actualPath.getFileName().toString();

    //         if (!currentNode.getName().equals(actualName)) {
    //             currentNode.setName(actualName);
    //             break;
    //         }

    //         actualPath = actualPath.getParent();

    //         if (currentNode.getParentId() == 0 || actualPath == null) {
    //             break;
    //         }

    //         currentNode = index.getNode(currentNode.getParentId());
    //     }

    //     System.out.println("\n\n");
    //     index.printTree();
    //     System.out.println("\n\n");
    //     System.out.println("\n\n");


    // }




    static void renameFileUpdation(
        UsnRecord record,
        AtlasState state
    ) {

        Index index = state.atlasIndex;

        // index.printTree();

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

        // 2. Move to its parent.
        // actualPath = actualPath.getParent();

        // if (actualPath == null) {
        //     return;
        // }

        FileSystemNode childNode = currentNode;

        System.out.println("childnodee: " + childNode.getName());

        currentNode = index.getNode(currentNode.getParentId());
        System.out.println("currentnodee: " + currentNode.getName());

        // 3. Reconcile ancestors using file IDs.
        while (currentNode != null) {

            try {
                Thread.sleep(1000); // Pause for 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            System.out.println("in while loop...");
            // currentNode = testmovefolder 
            System.out.println("currentnodee: " + currentNode.getName());

            Path actualParentPath = actualPath.getParent();

            // actualParentPath = C:\Users\asbia\Downloads\mealzz\another folder2\testfoldr12
            System.out.println("actualParentPathh: " + actualParentPath);

            if (actualParentPath == null) {
                break;
            }

            long actualParentId = ReadUsn.getFileId(actualParentPath);

            // actualParentId = testfoldr12 ka id
            System.out.println("actualParentIdd -> testfoldr12 ka id: " + actualParentId);
            System.out.println("for comparison if -> currentNode.getIdd() : " + currentNode.getId());


            // Parent relationship is correct. Continue upwards.
            ////// if (currentNode.getParentId() == actualParentId) {

            //     currentNode = index.getNode(actualParentId);
            //     actualPath = actualParentPath;

            //     continue;
            // }

            if (currentNode.getId() == actualParentId) {

                System.out.println("in comparison if -> currentNode.getIdd() : " + currentNode.getId());

                // currentNode = index.getNode(actualParentId);
                actualPath = actualParentPath;

                continue;
            }
            

            // Parent relationship differs. Reparent current node.
            FileSystemNode newParent = index.getNode(actualParentId);


            if (newParent == null) {
                System.out.println(
                    "Actual parent not found in index. Rescan required."
                );
                return;
            }


            // FileSystemNode oldParent = index.getNode(currentNode.getParentId());
            FileSystemNode oldParent = currentNode;

            // if (oldParent != null) {
            //     oldParent.removeChild(currentNode);
            // } else {
            //     index.getRootNodes().remove(currentNode);
            // }

            oldParent.removeChild(childNode);
            System.out.println("removing childnode: " + childNode.getName() + 
            "from oldparentt: " + oldParent.getName());

            childNode.setParentId(actualParentId);
            System.out.println("setting actualparentidd to childnode...");
            newParent.addChild(childNode);
            System.out.println("adding childnode: " + childNode.getName() + 
            "from newparentt: " + newParent.getName());

            System.out.println(
                "Successfully reconciled node: " + currentNode.getName()
            );

            break;
        }

        // System.out.println("\n\n");
        // index.printTree();
        // System.out.println("\n\n\n\n");
        
    }



    static void updateFileUpdation(AtlasState state) {
        System.out.println("dummy update file updation logic");
    }
}
