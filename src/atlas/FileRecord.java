package atlas;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FileRecord implements Serializable {
    
    String name;
    String path;
    long size;
    long createdAt;
    long modifiedAt;
    String extension;


    public FileRecord(Path path, BasicFileAttributes attribute) {

        this.name = path.getFileName().toString();
        this.path = path.toString();
        this.size = attribute.size();
        this.createdAt = attribute.creationTime().toMillis();
        this.modifiedAt = attribute.lastModifiedTime().toMillis();
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
        DateTimeFormatter formatter = 
            DateTimeFormatter.ofPattern("h:mm a, d MMMM yyyy")
                .withZone(ZoneId.systemDefault());
        builder.append("createdAt  : " + formatter.format(Instant.ofEpochMilli(file.createdAt)) + "\n");
        builder.append("modifiedAt : " + formatter.format(Instant.ofEpochMilli(file.modifiedAt)) + "\n");
        builder.append("extension  : " + file.extension + "\n");
        builder.append("}\n");
        
        String str = builder.toString();
        return str;
    }
}
