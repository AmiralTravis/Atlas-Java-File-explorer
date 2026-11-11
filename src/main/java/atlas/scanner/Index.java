package atlas.scanner;

import java.util.ArrayList;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;

import atlas.FileRecord;
import java.util.HashMap;
import java.util.Map;



public class Index implements Serializable {

    private ArrayList<FileRecord> fileList = new ArrayList<>();
    private ArrayList<String> folderList = new ArrayList<>();
    private ArrayList<String> directoryList = new ArrayList<>();

    private Map<Long, FileSystemNode> nodesById = new HashMap<>();

    private ArrayList<FileSystemNode> rootNodes = new ArrayList<>();


    public int filesIndexed;
    public int foldersIndexed;
    public int skippedItems;

    public LocalDateTime modifiedAt;

    public long lastProcessedUsn;



    void addFile(FileRecord fileRecord) {

        fileList.add(fileRecord);

    }

    void addFolder(Path folder) {

        folderList.add(folder.toString());

    }

    void addDirectory(Path dir) {

        directoryList.add(dir.toString());

    }

    public ArrayList<Path> getDirectoryList(Index atlasIndex) {
        ArrayList<Path> paths = new ArrayList<>();

        for (String dir : atlasIndex.directoryList) {
            paths.add(Path.of(dir));
        }

        return paths;
    } 


    public void addRootNode(FileSystemNode node) {
        nodesById.put(node.getId(), node);
        rootNodes.add(node);
    }

    public void addNode(FileSystemNode node, long parentId) {
        FileSystemNode parent = nodesById.get(parentId);

        if (parent == null) {
            throw new IllegalStateException(
                "Parent node not found: " + parentId
            );
        }

        parent.addChild(node);
        nodesById.put(node.getId(), node);
    }

    public FileSystemNode getNode(long id) {
        return nodesById.get(id);
    }

    public ArrayList<FileSystemNode> getRootNodes() {
        return rootNodes;
    }

    public Map<Long, FileSystemNode> getNodesById() {
        return nodesById;
    }



    public static void showFileRecords(ArrayList<FileRecord> fileList) {
        
        for (FileRecord i: fileList) {
            System.out.println(i.getFileRecord(i));
        }
        
        
    } 



    public void printTree() {
        for (FileSystemNode node : rootNodes) {
            printNode(node, "");
        }
    }

    private void printNode(FileSystemNode node, String indent) {

        System.out.println(
            " Name: " + indent + node.getName()
            + " | ID: " + node.getId()
            + " | Parent ID: " + node.getParentId()
            + " | Type: " + node.getType()
            + " | FileRecord: " + node.getFileRecord()
        );

        for (FileSystemNode child : node.getChildren().values()) {
            printNode(child, indent + "    ");
        }
    } 

}