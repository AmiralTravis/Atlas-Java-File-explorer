package atlas;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import atlas.scanner.ScanProgress;
import atlas.scanner.ScanResult;

public class AtlasState {

    Path currentPath;


    /*
     * Scan state
     */
    AtomicBoolean scanRunning =
        new AtomicBoolean(false);

    AtomicBoolean cancelScanRequested =
        new AtomicBoolean(false);

    /*
     * UI-only scanning flag.
     *
     * Unlike scanRunning (touched by the background
     * scanner thread), this is only ever read/written
     * by the main thread, so there's no race.
     */
    boolean uiScanning = false;

    ScanProgress scanProgress;

    volatile ScanResult scanResult;


    /*
     * Input communication.
     *
     * InputThread puts commands here.
     * Main thread takes commands from here.
     */
    final BlockingQueue<String> commandQueue =
        new LinkedBlockingQueue<>();


    /*
     * UI state.
     */
    volatile boolean uiNeedsRender = true;

    volatile String lastMessage = "";


    public AtlasState() {

        currentPath =
            Paths.get("\\");
    }
}