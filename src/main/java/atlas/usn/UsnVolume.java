package atlas.usn;

import java.nio.file.Path;
import java.util.ArrayList;

import atlas.AtlasState;
import atlas.scanner.Index;

public class UsnVolume {


    public static void updateIndexByUsnJournal(AtlasState state) {

        Index atlasIndex = state.atlasIndex;

        ArrayList<Path> targetDirectories = atlasIndex.getDirectoryList(atlasIndex);

        ArrayList<String> targetDirectoryStrings = new ArrayList<>();

        for (Path path : targetDirectories) {
            targetDirectoryStrings.add(path.toString());
        }

        long lastProcessedUsn = atlasIndex.lastProcessedUsn;

        // -read usn and get records
        UsnUpdateRecords updateRecord = ReadUsn.getUsnUpdates(targetDirectoryStrings, lastProcessedUsn);

        System.out.println("final rezzult: ");

        for (UsnRecord record : updateRecord.getRecords()) {
            System.out.println(record);
            System.out.println("---------------------------");
        }

        System.out.println("lastProcessedUsn high level reached: " 
        + updateRecord.getLastProcessedUsn());

        // -input records in index updation
	    // ->after successful updation, save atlas index, else report error
        UpdateIndex.updateIndexByUsn(updateRecord, state);

    }









}
