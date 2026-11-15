package atlas.usn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import atlas.AtlasState;
import atlas.FileRecord;
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
                newFileUpdation(usnUpdateRecord, state);
            }

            else if (usnUpdateRecord.getChangeType().equals("Deleted file")) {
                deleteFileUpdation(usnUpdateRecord, state);
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

    static void newFileUpdation(
        UsnRecord record,
        AtlasState state
    ) {

        Index atlasIndex = state.atlasIndex;

        // 1. Get file and parent IDs
        long fileId = record.getFileRef();
        long parentId = record.getParentFileRef();

        // 2. Check if node already exists
        if (atlasIndex.getNode(fileId) != null) {
            return;
        }

        // 3. Find parent node
        FileSystemNode parentNode = atlasIndex.getNode(parentId);

        if (parentNode == null) {
            System.out.println(
                "Parent node not found. Cannot add: " + record.getFullPath()
            );
            return;
        }

        // 4. Get actual path
        Path path = record.getFullPath();

        if (path == null) {
            return;
        }

        try {

            // 5. Read file attributes
            BasicFileAttributes attributes =
                Files.readAttributes(path, BasicFileAttributes.class);

            // 6. Determine node type
            boolean isDirectory = attributes.isDirectory();

            FileSystemNode.NodeType nodeType = isDirectory
                ? FileSystemNode.NodeType.DIRECTORY
                : FileSystemNode.NodeType.FILE;

            // 7. Create node
            FileSystemNode newNode = new FileSystemNode(
                fileId,
                parentId,
                path.getFileName().toString(),
                nodeType
            );

            // 8. Construct FileRecord only for files
            if (!isDirectory) {

                FileRecord fileRecord = new FileRecord(
                    path,
                    attributes
                );

                newNode.setFileRecord(fileRecord);

            }

            // 9. Add node to parent's children and nodesById
            atlasIndex.addNode(newNode, parentId);

            // System.out.println(
            //     "Added node: " + newNode.getName()
            //     + " | ID: " + fileId
            //     + " | Parent ID: " + parentId
            //     + " | Type: " + nodeType
            // );

        } catch (IOException e) {

            System.out.println(
                "Could not create index node for: " + path
            );

            e.printStackTrace();
        }
    }


    static void deleteFileUpdation(
        UsnRecord record,
        AtlasState state
    ) {

        Index atlasIndex = state.atlasIndex;

        // Get file ID from USN record
        Long fileId = record.getFileRef();

        // Find matching node in index
        FileSystemNode node = atlasIndex.getNode(fileId);

        if (node == null) {
            // System.out.println("Deleted file not found in index: " + fileId);
            return;
        }

        System.out.println("Deleting node: " + node.getName()
            + " | ID: " + node.getId());

        // Recursively delete node, its children,
        // and remove it from its parent's children.
        deleteNodeRecursively(atlasIndex, node);

    }


    static void renameFileUpdation(
        UsnRecord record,
        AtlasState state
    ) {

        Index index = state.atlasIndex;

        FileSystemNode currentNode = index.getNode(record.getFileRef());

        if (currentNode == null) {
            // handles rename from recylce bin to targetDir case (undo deletion case)
            newFileUpdation(record, state);
            return;
        }

        Path actualPath = record.getFullPath();

        // 1. Handle the original USN node separately.
        String actualName = actualPath.getFileName().toString();

        if (!currentNode.getName().equals(actualName)) {
            FileSystemNode parent = index.getNode(currentNode.getParentId());

            if (parent != null) {
                parent.removeChild(currentNode);
            }

            currentNode.setName(actualName);

            if (parent != null) {
                parent.addChild(currentNode);
            }
        }

        // 2. Move to its parent node logic.
        FileSystemNode childNode = currentNode;

        currentNode = index.getNode(currentNode.getParentId());

        // 3. Reconcile ancestors using file IDs.
        Path actualParentPath = actualPath.getParent();

        if (actualParentPath == null) {
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
        // System.out.println("dummy update file updation logic");
        // Do nothing for now
    }


    // helper methods:

    private static void deleteNodeRecursively(
        Index atlasIndex,
        FileSystemNode node) {

        // Delete all children first
        for (FileSystemNode child :
                new ArrayList<>(node.getChildren().values())) {

            deleteNodeRecursively(atlasIndex, child);
        }

        // Remove node from its parent's children
        FileSystemNode parent =
            atlasIndex.getNode(node.getParentId());

        if (parent != null) {
            parent.removeChild(node);
        }

        // Remove node from global ID lookup
        atlasIndex.getNodesById().remove(node.getId());

        // If it is a root node, remove it from rootNodes too
        atlasIndex.getRootNodes().remove(node);
    }




}
