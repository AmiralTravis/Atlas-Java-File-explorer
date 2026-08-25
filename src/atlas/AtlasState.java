package atlas;

import java.nio.file.Path;

public class AtlasState {

    Path currentPath;

    public AtlasState(Path startingPath) {
        this.currentPath = startingPath;
    }
}
