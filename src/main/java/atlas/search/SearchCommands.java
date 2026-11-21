package atlas.search;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atlas.AtlasState;
import atlas.scanner.FileSystemNode;
import atlas.scanner.Index;
import atlas.usn.ReadUsn;

public class SearchCommands {

    // ===================== SEARCH =====================

    public static void handleSearchByName(AtlasState state, String name) {

        if (state.atlasIndex == null) {
            System.out.println("No index available.\n");
            return;
        }

        List<SearchEngine.Match> results =
            SearchEngine.search(state.atlasIndex, name, state.currentFilters);

        if (results.isEmpty()) {
            System.out.println("No matching files found: " + name + "\n");
            return;
        }

        long exactCount = results.stream().filter(SearchEngine.Match::exact).count();

        System.out.println("\nFound " + exactCount + " exact and "
            + (results.size() - exactCount) + " similar matches.\n");

        boolean exactHeader = false;
        boolean fuzzyHeader = false;

        for (SearchEngine.Match m : results) {

            if (m.exact() && !exactHeader) {
                System.out.println("--- Exact matches ---\n");
                exactHeader = true;
            }

            if (!m.exact() && !fuzzyHeader) {
                System.out.println("--- Similar matches ---\n");
                fuzzyHeader = true;
            }

            FileSystemNode node = m.node();

            System.out.println(
                node.getName()
                + (m.exact() ? "" : String.format("  (%.0f%% match)", m.score() * 100))
                + "\n  Path: " + displayPath(node, state.atlasIndex)
                + "\n  Type: " + node.getType()
                + " | ID: " + node.getId()
                + "\n"
            );
        }
    }

    private static String displayPath(FileSystemNode node, Index index) {
        List<String> parts = new ArrayList<>();
        FileSystemNode current = node;
        int guard = 0;

        while (current != null && guard++ < 10000) {
            parts.add(0, current.getName());
            current = index.getNode(current.getParentId());
        }

        return String.join("\\", parts);
    }

    // ===================== FILTER MENUS =====================

    public static void handleChangeFilters(AtlasState state) {
        state.currentState = AtlasState.AppState.FILTER_MENU;
    }

    public static boolean isFilterState(AtlasState.AppState s) {
        return s == AtlasState.AppState.FILTER_MENU
            || s == AtlasState.AppState.EXT_FILTER_MENU
            || s == AtlasState.AppState.DATE_FILTER_MENU
            || s == AtlasState.AppState.SIZE_FILTER_MENU
            || s == AtlasState.AppState.DIR_FILTER_MENU;
    }

    public static void handleFilterInput(AtlasState state, String command) {

        String cmd = command.trim();

        switch (state.currentState) {
            case FILTER_MENU -> handleFilterMenu(state, cmd);
            case EXT_FILTER_MENU -> handleExtMenu(state, cmd);
            case DATE_FILTER_MENU -> handleDateMenu(state, cmd);
            case SIZE_FILTER_MENU -> handleSizeMenu(state, cmd);
            case DIR_FILTER_MENU -> handleDirMenu(state, cmd);
            default -> { }
        }
    }

    // returns text after "prefix " or null if cmd doesn't start with it
    private static String argAfter(String cmd, String prefix) {
        if (cmd.toLowerCase().startsWith(prefix + " ")) {
            return cmd.substring(prefix.length() + 1).trim();
        }
        return null;
    }

    private static void invalid() {
        System.out.println("invalid command\n");
    }

    // ---------- main filter menu ----------

    private static void handleFilterMenu(AtlasState state, String cmd) {
        switch (cmd.toLowerCase()) {
            case "edit extensions" -> state.currentState = AtlasState.AppState.EXT_FILTER_MENU;
            case "edit date range" -> state.currentState = AtlasState.AppState.DATE_FILTER_MENU;
            case "edit file size" -> state.currentState = AtlasState.AppState.SIZE_FILTER_MENU;
            case "edit directories" -> state.currentState = AtlasState.AppState.DIR_FILTER_MENU;
            case "clear all filters" -> {
                state.currentFilters.clearAll();
                System.out.println("All filters cleared.\n");
            }
            case "back" -> state.currentState = AtlasState.AppState.SEARCH_MENU;
            default -> invalid();
        }
    }

    // ---------- extensions ----------

    private static void handleExtMenu(AtlasState state, String cmd) {
        FilterState f = state.currentFilters;

        String addArg = argAfter(cmd, "add");
        String removeArg = argAfter(cmd, "remove");

        if (addArg != null) {
            for (String ext : addArg.split("[,\\s]+")) {
                String clean = ext.toLowerCase().replaceFirst("^\\.", "");
                if (clean.isEmpty()) continue;
                if (f.extensions.add(clean)) {
                    System.out.println("Added extension: " + clean);
                } else {
                    System.out.println("Already added: " + clean);
                }
            }
            System.out.println();
        }

        else if (removeArg != null) {
            for (String ext : removeArg.split("[,\\s]+")) {
                String clean = ext.toLowerCase().replaceFirst("^\\.", "");
                if (f.extensions.remove(clean)) {
                    System.out.println("Removed extension: " + clean);
                } else {
                    System.out.println("Not in filter: " + clean);
                }
            }
            System.out.println();
        }

        else if (cmd.equalsIgnoreCase("clear")) {
            f.extensions.clear();
            System.out.println("Extension filter cleared.\n");
        }

        else if (cmd.equalsIgnoreCase("back")) {
            state.currentState = AtlasState.AppState.FILTER_MENU;
        }

        else {
            invalid();
        }
    }

    // ---------- date range ----------

    private static void handleDateMenu(AtlasState state, String cmd) {
        FilterState f = state.currentFilters;

        String fromArg = argAfter(cmd, "from");
        String toArg = argAfter(cmd, "to");

        try {
            if (fromArg != null) {
                LocalDate d = LocalDate.parse(fromArg);
                if (f.dateTo != null && d.isAfter(f.dateTo)) {
                    System.out.println("'from' date can't be after 'to' date.\n");
                    return;
                }
                f.dateFrom = d;
                System.out.println("Date from set to " + d + "\n");
            }

            else if (toArg != null) {
                LocalDate d = LocalDate.parse(toArg);
                if (f.dateFrom != null && d.isBefore(f.dateFrom)) {
                    System.out.println("'to' date can't be before 'from' date.\n");
                    return;
                }
                f.dateTo = d;
                System.out.println("Date to set to " + d + "\n");
            }

            else if (cmd.equalsIgnoreCase("clear")) {
                f.dateFrom = null;
                f.dateTo = null;
                System.out.println("Date filter cleared.\n");
            }

            else if (cmd.equalsIgnoreCase("back")) {
                state.currentState = AtlasState.AppState.FILTER_MENU;
            }

            else {
                invalid();
            }

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date. Use format YYYY-MM-DD, e.g. 2024-06-30\n");
        }
    }

    // ---------- size ----------

    private static final Pattern SIZE_PATTERN =
        Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*(b|kb|mb|gb|tb)?$", Pattern.CASE_INSENSITIVE);

    private static Long parseSize(String s) {
        Matcher m = SIZE_PATTERN.matcher(s.trim());
        if (!m.matches()) return null;

        double value = Double.parseDouble(m.group(1));
        String unit = m.group(2) == null ? "b" : m.group(2).toLowerCase();

        long multiplier = switch (unit) {
            case "kb" -> 1L << 10;
            case "mb" -> 1L << 20;
            case "gb" -> 1L << 30;
            case "tb" -> 1L << 40;
            default -> 1L;
        };

        return (long) (value * multiplier);
    }

    private static void handleSizeMenu(AtlasState state, String cmd) {
        FilterState f = state.currentFilters;

        String minArg = argAfter(cmd, "min");
        String maxArg = argAfter(cmd, "max");

        if (minArg != null || maxArg != null) {

            boolean isMin = minArg != null;
            Long size = parseSize(isMin ? minArg : maxArg);

            if (size == null) {
                System.out.println("Invalid size. Examples: 500, 10KB, 5MB, 1.5GB\n");
                return;
            }

            if (isMin) {
                if (f.maxSize != null && size > f.maxSize) {
                    System.out.println("Min size can't be greater than max size.\n");
                    return;
                }
                f.minSize = size;
                System.out.println("Min size set to " + FilterState.formatSize(size) + "\n");
            } else {
                if (f.minSize != null && size < f.minSize) {
                    System.out.println("Max size can't be smaller than min size.\n");
                    return;
                }
                f.maxSize = size;
                System.out.println("Max size set to " + FilterState.formatSize(size) + "\n");
            }
        }

        else if (cmd.equalsIgnoreCase("clear")) {
            f.minSize = null;
            f.maxSize = null;
            System.out.println("Size filter cleared.\n");
        }

        else if (cmd.equalsIgnoreCase("back")) {
            state.currentState = AtlasState.AppState.FILTER_MENU;
        }

        else {
            invalid();
        }
    }

    // ---------- directories ----------

    private static void handleDirMenu(AtlasState state, String cmd) {
        FilterState f = state.currentFilters;

        String addArg = argAfter(cmd, "add");
        String removeArg = argAfter(cmd, "remove");

        if (addArg != null) {

            try {
                Path path = Path.of(addArg).toAbsolutePath().normalize();

                if (!Files.isDirectory(path)) {
                    System.out.println("Not an existing directory: " + path + "\n");
                    return;
                }

                long id = ReadUsn.getFileId(path);

                if (state.atlasIndex == null || state.atlasIndex.getNode(id) == null) {
                    System.out.println("This directory is not part of the index: " + path + "\n");
                    return;
                }

                if (f.directories.containsKey(id)) {
                    System.out.println("Already added: " + path + "\n");
                    return;
                }

                f.directories.put(id, path);
                System.out.println("Added directory: " + path + "\n");

            } catch (Exception e) {
                System.out.println("Could not add directory: " + e.getMessage() + "\n");
            }
        }

        else if (removeArg != null) {

            try {
                Path path = Path.of(removeArg).toAbsolutePath().normalize();

                boolean removed = f.directories.values().removeIf(p -> p.equals(path));

                System.out.println(removed
                    ? "Removed directory: " + path + "\n"
                    : "Not in filter: " + path + "\n");

            } catch (Exception e) {
                System.out.println("Invalid path.\n");
            }
        }

        else if (cmd.equalsIgnoreCase("clear")) {
            f.directories.clear();
            System.out.println("Directory filter cleared.\n");
        }

        else if (cmd.equalsIgnoreCase("back")) {
            state.currentState = AtlasState.AppState.FILTER_MENU;
        }

        else {
            invalid();
        }
    }
}