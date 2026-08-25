package atlas.scanner;

import java.util.concurrent.atomic.AtomicInteger;

public class ScanProgress {

    private final AtomicInteger filesFound = new AtomicInteger();
    private final AtomicInteger foldersFound = new AtomicInteger();

    private volatile String currentPath = "";

    public void incrementFiles() {
        filesFound.incrementAndGet();
    }

    public void incrementFolders() {
        foldersFound.incrementAndGet();
    }

    public int getFilesFound() {
        return filesFound.get();
    }

    public int getFoldersFound() {
        return foldersFound.get();
    }

    public void setCurrentPath(String path) {
        currentPath = path;
    }

    public String getCurrentPath() {
        return currentPath;
    }
}