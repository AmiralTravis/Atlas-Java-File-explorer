package atlas.scanner;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileScanner {

    public ScanResult scan(
        Path root,
        ScanProgress progress,
        AtomicBoolean cancelRequested
    ) {

        ScanResult result = new ScanResult();

        try {

            Files.walkFileTree(
                root,
                new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(
                        Path directory,
                        BasicFileAttributes attributes
                    ) {

                        if (cancelRequested.get()) {
                            return FileVisitResult.TERMINATE;
                        }

                        result.incrementFolders();
                        progress.incrementFolders();
                        progress.setCurrentPath(directory.toString());

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                        Path file,
                        BasicFileAttributes attributes
                    ) {

                        if (cancelRequested.get()) {
                            return FileVisitResult.TERMINATE;
                        }

                        result.incrementFiles();
                        progress.incrementFiles();
                        progress.setCurrentPath(file.toString());

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(
                        Path file,
                        IOException exception
                    ) {

                        result.incrementSkipped();
                        progress.incrementSkipped();

                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
            );

        } catch (IOException e) {

            System.err.println(
                "Scan could not be completed: "
                + e.getMessage()
            );
        }

        return result;
    }

}