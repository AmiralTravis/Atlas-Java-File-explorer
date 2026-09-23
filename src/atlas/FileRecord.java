package atlas;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;;

public class FileRecord {
    
    String name;
    Path path;
    long size;
    FileTime createdAt;
    FileTime modifiedAt;
    String extension;


    public FileRecord(Path path, BasicFileAttributes attribute) {

        this.name = path.getFileName().toString();
        this.path = path;
        this.size = attribute.size();
        this.createdAt = attribute.creationTime();
        this.modifiedAt = attribute.lastModifiedTime();
        this.extension = getExtension(this.name);
    
    }

    private String getExtension(String fileName) {

        int dot = fileName.lastIndexOf('.');

        if (dot == -1 || dot == fileName.length() - 1) {
            return "";
        }

        return fileName.substring( dot + 1);

    }

    public String getFileRecord(FileRecord file) {
        
        StringBuilder builder = new StringBuilder();

        builder.append("{\n");
        builder.append("name       : " + file.name + "\n");
        builder.append("path       : " + file.path + "\n");
        builder.append("size       : " + file.size + "\n");
        builder.append("createdAt  : " + file.createdAt + "\n");
        builder.append("modifiedAt : " + file.modifiedAt + "\n");
        builder.append("extension  : " + file.extension + "\n");
        builder.append("}\n");
        
        String str = builder.toString();
        return str;
    }
}
