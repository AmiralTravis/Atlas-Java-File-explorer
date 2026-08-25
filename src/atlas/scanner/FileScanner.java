package atlas.scanner;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileScanner {

    public ScanResult scan(Path root) {

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

                        result.incrementFolders();

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                        Path file,
                        BasicFileAttributes attributes
                    ) {

                        result.incrementFiles();

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(
                        Path file,
                        IOException exception
                    ) {

                        System.err.println(
                            "Could not access: "
                            + file
                            + " — "
                            + exception.getMessage()
                        );

                        result.incrementSkipped();

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