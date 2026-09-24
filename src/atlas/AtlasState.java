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
        INDEX_MENU,
        BROWSE_MENU,
        EXITING
    }

    public State currentState = State.MAIN_MENU;

    public BlockingQueue<QueueItem> actionQueue = new LinkedBlockingQueue<>();

    public Path currentPath = Paths.get("\\users\\asbia\\downloads\\mealzz");

    public volatile Path currentScanPath = Paths.get("\\");
    public volatile ScanResult currentScanResult; // the scan's data/progress

    public Future<?> scanFuture; // control over the scan task (cancel, check done, etc.)

    public Index atlasIndex;

    AtlasState() {
        this(null);
    }


    AtlasState(Index atlasIndex) {
        this.atlasIndex = atlasIndex;
    }



}




