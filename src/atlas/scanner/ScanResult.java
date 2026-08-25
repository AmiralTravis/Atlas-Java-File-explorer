package atlas.scanner;

public class ScanResult {
    
    private int filesFound;
    private int foldersFound;
    private int skippedFound;

    public void incrementFiles() {
        filesFound++;
    }

    public void incrementFolders() {
        foldersFound++;
    }

    public void incrementSkipped() {
        skippedFound++;
    }

    public int getFilesFound() {
        return filesFound;
    }

    public int getFoldersFound() {
        return foldersFound;
    }

    public int getSkippedFound() {
        return skippedFound;
    }
}
