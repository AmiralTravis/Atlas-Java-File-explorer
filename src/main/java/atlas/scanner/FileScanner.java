package atlas.scanner;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import atlas.FileRecord;

public class FileScanner {


    public static ScanResult scan(Path root, ScanResult result, Index atlasIndex) {

        try {

            Files.walkFileTree(
                root,
                new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(
                        Path directory,
                        BasicFileAttributes attributes
                    ) {

                        if (Thread.currentThread().isInterrupted()) {
                            return FileVisitResult.TERMINATE;
                        }

                        result.incrementFolders();

                        // System.out.println(
                        //     "passed result.incrementFolders"
                        // );

                        atlasIndex.addFolder(directory);
                        // System.out.println(
                        //     "passed atlasIndex.addFolder(directory)"
                        // );

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                        Path file,
                        BasicFileAttributes attributes
                    ) {

                        if (Thread.currentThread().isInterrupted()) {
                            return FileVisitResult.TERMINATE;
                        }

                        result.incrementFiles();

                        // System.out.println(
                        //     "passed result.incrementFiles();"
                        // );

                        FileRecord record = new FileRecord(file, attributes);
                        // System.out.println(
                        //     "passed FileRecord record = new FileRecord(file, attributes);"
                        // );

                        atlasIndex.addFile(record);
                        // System.out.println(
                        //     "passed atlasIndex.addFile(record);"
                        // );

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

                        if (Thread.currentThread().isInterrupted()) {
                            return FileVisitResult.TERMINATE;
                        }

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