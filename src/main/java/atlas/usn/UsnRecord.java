package atlas.usn;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;


public class UsnRecord {
    


// file name
// file path
// file size
// timestamp ist(created at and updated at)
// rreason
// mapping

    private String name;
    private Path path;
    private long size;
    private long createdAt;
    private long modifiedAt;
    private String extension;
    private String reason;
    private String changeType;

    private long fileRef;
    private long parentFileRef;

   
    public String getChangeType() {
        return this.changeType;
    }

    public UsnRecord(Path path, BasicFileAttributes attributes,
                String reason, String changeType,
                long fileRef, long parentFileRef) {

        this.name = path.getFileName().toString();
        this.path = path;
        this.size = attributes.size();
        this.createdAt = attributes.creationTime().toMillis();
        this.modifiedAt = attributes.lastModifiedTime().toMillis();
        this.extension = getExtension(this.name);
        this.reason = reason;
        this.changeType = changeType;
        
        this.fileRef = fileRef;
        this.parentFileRef = parentFileRef;
    }


    public long getFileRef() {
        return fileRef;
    }

    public long getParentFileRef() {
        return parentFileRef;
    }


    private String getExtension(String fileName) {

        int dot = fileName.lastIndexOf('.');

        if (dot == -1 || dot == fileName.length() - 1) {
            return "";
        }

        return fileName.substring( dot + 1);

    }



    @Override
public String toString() {
    return "Name: " + name +
           "\nPath: " + path +
           "\nSize: " + size +
           "\nCreated At: " + createdAt +
           "\nModified At: " + modifiedAt +
           "\nExtension: " + extension +
           "\nReason: " + reason +
           "\nChange Type: " + changeType;
}


}
