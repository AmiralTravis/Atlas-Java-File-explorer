package atlas.scanner;

import java.util.ArrayList;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;

import atlas.FileRecord;

public class Index implements Serializable {

    private ArrayList<FileRecord> fileList = new ArrayList<>();
    private ArrayList<String> folderList = new ArrayList<>();
    private ArrayList<String> directoryList = new ArrayList<>();

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

    void addDirectory(Path dir) {

        directoryList.add(dir.toString());

    }

    public static ArrayList<Path> getDirectoryList(Index atlasIndex) {
        ArrayList<Path> paths = new ArrayList<>();

        for (String dir : atlasIndex.directoryList) {
            paths.add(Path.of(dir));
        }

        return paths;
    } 

    public static void showFileRecords(ArrayList<FileRecord> fileList) {
        
        for (FileRecord i: fileList) {
            System.out.println(i.getFileRecord(i));
        }
        
        
    } 

}