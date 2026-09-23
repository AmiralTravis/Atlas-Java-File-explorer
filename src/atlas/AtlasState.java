package atlas;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.file.Path;
import java.nio.file.Paths;

import atlas.scanner.ScanResult;
import atlas.scanner.Index;

public class AtlasState {

    public enum State {
        MAIN_MENU,
        SCANNING,
        EXITING
    }

    public State currentState = State.MAIN_MENU;

    public BlockingQueue<QueueItem> actionQueue = new LinkedBlockingQueue<>();

    public Path currentPath = Paths.get("\\users\\asbia\\downloads");

    public volatile Path currentScanPath = null;
    public volatile ScanResult currentScanResult; // the scan's data/progress

    public Future<?> scanFuture; // control over the scan task (cancel, check done, etc.)

    public Index atlasIndex;

}




