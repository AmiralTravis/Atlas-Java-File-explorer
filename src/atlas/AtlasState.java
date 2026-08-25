package atlas;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import atlas.scanner.ScanProgress;
import atlas.scanner.ScanResult;

public class AtlasState {

    Path currentPath;

    AtomicBoolean scanRunning =
        new AtomicBoolean(false);

    AtomicBoolean cancelScanRequested =
        new AtomicBoolean(false);

    ScanProgress scanProgress;

    volatile ScanResult scanResult;


    public AtlasState() {

        this.currentPath =
            Paths.get("\\");
    }
}