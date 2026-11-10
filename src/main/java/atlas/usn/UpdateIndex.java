package atlas.usn;

import java.io.IOException;
import java.nio.file.Path;

import atlas.AtlasState;
import atlas.scanner.IndexStore;

public class UpdateIndex {
    
    static void updateIndexByUsn(UsnUpdateRecords updateRecord, AtlasState state) {

        System.out.println("dummy update index by usn...");


        try {

            state.atlasIndex.lastProcessedUsn = updateRecord.lastProcessedUsn;
    
            Path indexPath = state.indexPath;
            
            IndexStore.writeIndex(state.atlasIndex, indexPath);

        } catch (IOException e) {

            System.err.println("Failed to save Index to disk");
            e.printStackTrace();

        }


        
    }
}
