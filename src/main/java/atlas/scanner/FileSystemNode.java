
package atlas.scanner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import atlas.FileRecord;

public class FileSystemNode implements Serializable {

    public enum NodeType {
        FILE,
        DIRECTORY
    }

    private long id;
    private long parentId;

    private String name;
    private NodeType type;

    // private FileSystemNode parent;

    private Map<String, FileSystemNode> children = new HashMap<>();

    private FileRecord fileRecord;


    public FileSystemNode(long id, long parentId, String name, NodeType type) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
    }


    // Getters

    public long getId() {
        return id;
    }

    public long getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public NodeType getType() {
        return type;
    }

    public Map<String, FileSystemNode> getChildren() {
        return children;
    }

    public FileRecord getFileRecord() {
        return fileRecord;
    }


    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public void setFileRecord(FileRecord fileRecord) {
        this.fileRecord = fileRecord;
    }


    public void addChild(FileSystemNode child) {

        // Check if a child with the same file ID already exists
        for (FileSystemNode existingChild : children.values()) {
            if (existingChild.getId() == child.getId()) {
                return; // Already exists, don't add again
            }
        }

        children.put(child.getName(), child);
        child.setParentId(this.id);
    }

    public FileSystemNode removeChild(String childName) {
        FileSystemNode child = children.remove(childName);

        if (child != null) {
            child.setParentId(-1L);
        }

        return child;
    }

    public void removeChild(FileSystemNode child) {
        removeChild(child.getName());
    }

    public void setType(NodeType type) {
        this.type = type;
    }

}
