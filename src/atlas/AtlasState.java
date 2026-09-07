package atlas;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import atlas.scanner.ScanProgress;
import atlas.scanner.ScanResult;

public class AtlasState {

    public Path currentPath;


    /*
     * Scan state
     */
    public AtomicBoolean scanRunning =
        new AtomicBoolean(false);

    public AtomicBoolean cancelScanRequested =
        new AtomicBoolean(false);


    /*
     * UI-only scanning flag.
     *
     * Unlike scanRunning (touched by the background
     * scanner thread), this is only ever read/written
     * by the main thread, so there's no race.
     */
    public boolean uiScanning = false;

    public ScanProgress scanProgress;

    public volatile ScanResult scanResult;


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
    public volatile boolean uiNeedsRender = true;

    public volatile String lastMessage = "";


    public AtlasState() {

        currentPath =
            Paths.get("\\");
    }
}