package atlas.scanner;

import java.util.ArrayList;
import java.nio.file.Path;

import atlas.FileRecord;

public class Index {

    public ArrayList<FileRecord> fileList = new ArrayList<>();
    public ArrayList<Path> folderList = new ArrayList<>();


    void addFile(FileRecord fileRecord) {

        this.fileList.add(fileRecord);

    }

    void addFolder(Path folder) {
        // System.out.println("entered addfoldeR:");
        // System.out.println("start folder: " + folder);
        // System.out.println("start folderList: " + folderList);
        folderList.add(folder);
        // this.folderList.add(folder);
        // System.out.println("folderList: " + folderList);
        // System.out.println("exiitng addoflder");

    }

    public static void showFileRecords(ArrayList<FileRecord> fileList) {
        
        for (FileRecord i: fileList) {
            System.out.println(i.getFileRecord(i));
        }
        
        
    } 

}