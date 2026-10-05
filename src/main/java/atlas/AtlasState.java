package atlas;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.file.Path;
import java.nio.file.Paths;

import atlas.scanner.ScanResult;
import atlas.scanner.Index;

public class AtlasState {

    public enum AppState {
        MAIN_MENU,
        SCANNING,
        INDEX_MENU,
        BROWSE_MENU,
        EXITING
    }

    public enum IndexMenuState {
        NO_INDEX,
        WITH_INDEX,
        UPDATE_INDEX
    }

    public enum ConfirmationMenuState {
        NONE,
        DELETE_INDEX,
        RESCAN
    }

    public AppState currentState = AppState.MAIN_MENU;
    public IndexMenuState indexState = IndexMenuState.NO_INDEX;
    public ConfirmationMenuState confState = ConfirmationMenuState.NONE; 

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

    public final Path indexPath = Paths.get("atlas.index");

    public ArrayList<String> currentCommands = new ArrayList<>(List.of("exit"));

}




