package atlas.usn;

import atlas.AtlasState;
import atlas.scanner.Index;

public class AutoUsnUpdater {

    private static final Object WAKE = new Object();
    private static boolean signal = false;
    private static Thread thread;

    // call once at startup; the thread lives until the app exits
    public static synchronized void start(AtlasState state) {

        if (thread != null) return;

        thread = new Thread(() -> run(state), "atlas-auto-usn");
        thread.setDaemon(true);
        thread.start();
    }

    // call whenever a new index becomes available
    public static void notifyIndexReady() {
        synchronized (WAKE) {
            signal = true;
            WAKE.notifyAll();
        }
    }

    private static void awaitSignal() throws InterruptedException {
        synchronized (WAKE) {
            while (!signal) {
                WAKE.wait();
            }
            signal = false;
        }
    }

    private static void run(AtlasState state) {

        while (true) {

            try {

                Index index;
                long startUsn;

                synchronized (state.indexLock) {
                    index = state.atlasIndex;
                    startUsn = index == null ? 0 : index.lastProcessedUsn;
                }

                if (index == null) {
                    awaitSignal();   // no index: sleep until a scan finishes
                    continue;
                }

                // blocks until the journal has something new
                boolean hasChanges = ReadUsn.waitForUsnChange(startUsn);

                if (!hasChanges) {
                    Thread.sleep(250); // safety valve: never spin if the wait returns empty
                    continue;
                }

                long before = 0;
                long after = 0;

                synchronized (state.indexLock) {

                    Index current = state.atlasIndex; // re-read: may have changed while waiting

                    if (current == null) {
                        continue;
                    }

                    before = current.lastProcessedUsn;

                    UsnVolume.updateIndexByUsnJournal(state);

                    after = current.lastProcessedUsn;
                }

                if (after == before) {
                    // update bailed out without consuming records; retrying would spam
                    System.err.println(
                        "Auto USN update made no progress. Pausing until the index changes.\n"
                    );
                    awaitSignal();
                }

            } catch (InterruptedException e) {

                return;

            } catch (Throwable t) {

                System.err.println("\nAuto USN update failed:");
                t.printStackTrace();
                System.err.println("Auto updates paused until the index changes.\n");

                try {
                    awaitSignal();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}