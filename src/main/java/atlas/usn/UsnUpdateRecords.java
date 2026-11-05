package atlas.usn;

import java.util.ArrayList;

public class UsnUpdateRecords {
    
    ArrayList<UsnRecord> UsnUpdateRecordsList = new ArrayList<>();

    long lastProcessedUsn;

    public void addRecord(UsnRecord record) {
        UsnUpdateRecordsList.add(record);
    }

    public ArrayList<UsnRecord> getRecords() {
        return UsnUpdateRecordsList;
    }

    public void addLastProcessedUsn(UsnUpdateRecords records, long lastProcessedUsn) {
        this.lastProcessedUsn = lastProcessedUsn;
    }

    public long getLastProcessedUsn() {
        return lastProcessedUsn;
    }

}

