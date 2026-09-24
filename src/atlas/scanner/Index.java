package atlas.scanner;

import java.util.ArrayList;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;

import atlas.FileRecord;

public class Index implements Serializable {

    public ArrayList<FileRecord> fileList = new ArrayList<>();
    public ArrayList<String> folderList = new ArrayList<>();

    public int filesIndexed;
    public int foldersIndexed;
    public int skippedItems;

    public LocalDateTime modifiedAt;


    void addFile(FileRecord fileRecord) {

        fileList.add(fileRecord);

    }

    void addFolder(Path folder) {

        folderList.add(folder.toString());

    }

    public static void showFileRecords(ArrayList<FileRecord> fileList) {
        
        for (FileRecord i: fileList) {
            System.out.println(i.getFileRecord(i));
        }
        
        
    } 

}