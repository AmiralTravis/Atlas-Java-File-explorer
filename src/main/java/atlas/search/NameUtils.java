package atlas.search;

import atlas.scanner.FileSystemNode;

public final class NameUtils {

    private NameUtils() {}

    public static String baseName(String name) {
        String lower = name.toLowerCase();
        int dot = lower.lastIndexOf('.');
        return dot <= 0 ? lower : lower.substring(0, dot);
    }

    public static String extension(String name) {
        String lower = name.toLowerCase();
        int dot = lower.lastIndexOf('.');
        if (dot <= 0 || dot == lower.length() - 1) {
            return "";
        }
        return lower.substring(dot + 1);
    }

    // The key used in Index.nodeIdsByName
    public static String key(FileSystemNode node) {
        if (node.getType() == FileSystemNode.NodeType.FILE) {
            return baseName(node.getName());
        }
        return node.getName().toLowerCase();
    }
}