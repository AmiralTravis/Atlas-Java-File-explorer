package atlas.scanner;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


import atlas.FileRecord;
import atlas.usn.ReadUsn;

public class FileScanner {


    public static ScanResult scan(List<Path> paths, ScanResult result, Index atlasIndex) {

        try {

            for (Path path : paths) {

                Map<Path, Long> directoryIds = new HashMap<>();

                atlasIndex.addDirectory(path);
                
                Files.walkFileTree(
                    path,
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

                            long directoryId = ReadUsn.getFileId(directory);

                            long parentReferId = ReadUsn.getParentFileId(directory);

                            directoryIds.put(directory, directoryId);

                            FileSystemNode node = new FileSystemNode(
                                directoryId,
                                parentReferId,
                                directory.getFileName() == null
                                    ? directory.toString()
                                    : directory.getFileName().toString(),
                                FileSystemNode.NodeType.DIRECTORY
                            );

                            Path parentPath = directory.getParent();

                            Long parentId = directoryIds.get(parentPath);

                            if (parentId == null) {
                                atlasIndex.addRootNode(node);
                            } else {
                                atlasIndex.addNode(node, parentId);
                            }

                            atlasIndex.addFolder(directory);

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

                            long fileId = ReadUsn.getFileId(file);

                            long parentReferId = ReadUsn.getParentFileId(file);

                            Long parentId = directoryIds.get(file.getParent());

                            if (parentId == null) {
                                throw new IllegalStateException(
                                    "Parent directory not found in index: " + file
                                );
                            }

                            FileRecord record = new FileRecord(file, attributes);

                            FileSystemNode node = new FileSystemNode(
                                fileId,
                                parentReferId,
                                file.getFileName().toString(),
                                FileSystemNode.NodeType.FILE
                            );

                            node.setFileRecord(record);

                            atlasIndex.addNode(node, parentId);

                            atlasIndex.addFile(record);

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

            }

            

        } catch (IOException e) {

            System.err.println(
                "Scan could not be completed: "
                + e.getMessage()
            );
        }

        return result;
    }

}