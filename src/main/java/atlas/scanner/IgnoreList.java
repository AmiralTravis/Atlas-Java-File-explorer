package atlas.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IgnoreList {

    private static final Path FILE = Path.of("atlas.ignore");

    private static final List<String> DEFAULTS = List.of(
        "# Directories skipped when scanning. One entry per line.",
        "# A plain name (node_modules) is skipped everywhere.",
        "# A full path (C:\\Users\\me\\junk) skips just that folder.",
        "",
        "# JavaScript / Node",
        "node_modules",
        ".next",
        ".nuxt",
        "bower_components",
        "",
        "# Python",
        "__pycache__",
        ".venv",
        "venv",
        "env",
        ".tox",
        ".mypy_cache",
        ".pytest_cache",
        "site-packages",
        "",
        "# Version control / IDE",
        ".git",
        ".svn",
        ".hg",
        ".idea",
        ".vscode",
        "",
        "# Build output",
        "target",
        "build",
        "dist",
        "out",
        "bin",
        "obj",
        "",
        "# Windows system",
        "$Recycle.Bin",
        "System Volume Information"
    );

    private final Set<String> names = new HashSet<>();
    private final Set<Path> paths = new HashSet<>();

    private IgnoreList() {}

    public static IgnoreList load() {

        IgnoreList list = new IgnoreList();

        try {

            if (!Files.exists(FILE)) {
                Files.write(FILE, DEFAULTS);
                System.out.println("Created default ignore list: " + FILE.toAbsolutePath() + "\n");
            }

            for (String raw : Files.readAllLines(FILE)) {

                String line = raw.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.contains("\\") || line.contains("/") || line.contains(":")) {
                    list.paths.add(Path.of(line).toAbsolutePath().normalize());
                } else {
                    list.names.add(line.toLowerCase());
                }
            }

        } catch (IOException | RuntimeException e) {
            System.err.println("Could not read " + FILE + ": " + e.getMessage());
        }

        return list;
    }

    // used during scanning: checks a single directory
    public boolean isIgnoredDir(Path dir) {

        Path name = dir.getFileName();

        if (name != null && names.contains(name.toString().toLowerCase())) {
            return true;
        }

        return startsWithIgnoredPath(dir);
    }

    // used for USN updates: checks every segment of a full path
    public boolean isIgnoredPath(Path path) {

        for (Path segment : path) {
            if (names.contains(segment.toString().toLowerCase())) {
                return true;
            }
        }

        return startsWithIgnoredPath(path);
    }

    private boolean startsWithIgnoredPath(Path path) {

        Path normalized = path.toAbsolutePath().normalize();

        for (Path ignored : paths) {
            if (normalized.startsWith(ignored)) {
                return true;
            }
        }

        return false;
    }
}