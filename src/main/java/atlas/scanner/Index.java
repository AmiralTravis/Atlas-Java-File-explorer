package atlas.scanner;

import java.util.ArrayList;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class Index implements Serializable {

    private ArrayList<String> directoryList = new ArrayList<>();

    private Map<Long, FileSystemNode> nodesById = new HashMap<>();
    private Map<String, Set<Long>> nodeIdsByName = new HashMap<>();
    
    private ArrayList<FileSystemNode> rootNodes = new ArrayList<>();


    public int filesIndexed;
    public int foldersIndexed;
    public int skippedItems;

    public LocalDateTime modifiedAt;

    public long lastProcessedUsn;


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

        indexName(node);
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

        indexName(node);
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



    private void indexName(FileSystemNode node) {
        nodeIdsByName
            .computeIfAbsent(node.getName(), k -> new HashSet<>())
            .add(node.getId());
    }


    private void removeName(FileSystemNode node) {
        Set<Long> ids = nodeIdsByName.get(node.getName());

        if (ids != null) {
            ids.remove(node.getId());

            if (ids.isEmpty()) {
                nodeIdsByName.remove(node.getName());
    
            }
        }
    }


    public Map<String, Set<Long>> getNodeIdsByName() {
        return nodeIdsByName;
    }



    public Set<FileSystemNode> searchByName(String name) {

        System.out.println("getnodesidbyname: " + getNodeIdsByName());
        
        Set<Long> ids = nodeIdsByName.get(name);

        if (ids == null) {
            return Set.of();
        }

        Set<FileSystemNode> results = new HashSet<>();

        for (Long id : ids) {

            FileSystemNode node = nodesById.get(id);

            if (node != null) {
                results.add(node);
            }
        }

        return results;
    }



    public void removeNodeFromIndex(FileSystemNode node) {

        nodesById.remove(node.getId());

        removeName(node);
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