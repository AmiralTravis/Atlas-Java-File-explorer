package atlas.search;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import atlas.FileRecord;
import atlas.scanner.FileSystemNode;
import atlas.scanner.Index;

public class FilterState {

    public final Set<String> extensions = new LinkedHashSet<>();
    public LocalDate dateFrom;
    public LocalDate dateTo;
    public Long minSize;   // bytes
    public Long maxSize;   // bytes
    public final Map<Long, Path> directories = new LinkedHashMap<>(); // dirNodeId -> path

    public void clearAll() {
        extensions.clear();
        dateFrom = null;
        dateTo = null;
        minSize = null;
        maxSize = null;
        directories.clear();
    }

    public boolean matches(FileSystemNode node, Index index) {

        boolean isFile = node.getType() == FileSystemNode.NodeType.FILE;

        // extension filter
        if (!extensions.isEmpty()) {
            if (!isFile || !extensions.contains(NameUtils.extension(node.getName()))) {
                return false;
            }
        }

        // size + date filters (only meaningful for files)
        if (dateFrom != null || dateTo != null || minSize != null || maxSize != null) {

            FileRecord record = node.getFileRecord();

            if (!isFile || record == null) {
                return false;
            }

            long size = sizeOf(record);

            if (minSize != null && size < minSize) return false;
            if (maxSize != null && size > maxSize) return false;

            LocalDate modified = modifiedOf(record);

            if (dateFrom != null && modified.isBefore(dateFrom)) return false;
            if (dateTo != null && modified.isAfter(dateTo)) return false;
        }

        // directory filter
        if (!directories.isEmpty() && !isUnderSelectedDirectory(node, index)) {
            return false;
        }

        return true;
    }

    private boolean isUnderSelectedDirectory(FileSystemNode node, Index index) {
        FileSystemNode current = node;
        int guard = 0;

        while (current != null && guard++ < 10000) {
            if (directories.containsKey(current.getId())) {
                return true;
            }
            current = index.getNode(current.getParentId());
        }

        return false;
    }

    private static long sizeOf(FileRecord record) {
        return record.getSize();
    }

    private static LocalDate modifiedOf(FileRecord record) {
        return Instant.ofEpochMilli(record.getModifiedAt())
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double v = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int i = -1;
        while (v >= 1024 && i < units.length - 1) {
            v /= 1024;
            i++;
        }
        return String.format("%.1f %s", v, units[i]);
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("  Extensions : ")
          .append(extensions.isEmpty() ? "any" : String.join(", ", extensions))
          .append("\n");

        sb.append("  Date range : ")
          .append(dateFrom == null ? "any" : dateFrom)
          .append(" -> ")
          .append(dateTo == null ? "any" : dateTo)
          .append("\n");

        sb.append("  File size  : ")
          .append(minSize == null ? "any" : formatSize(minSize))
          .append(" -> ")
          .append(maxSize == null ? "any" : formatSize(maxSize))
          .append("\n");

        sb.append("  Directories: ");
        if (directories.isEmpty()) {
            sb.append("all\n");
        } else {
            sb.append("\n");
            for (Path p : directories.values()) {
                sb.append("    ").append(p).append("\n");
            }
        }

        return sb.toString();
    }
}