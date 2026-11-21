package atlas.search;

import atlas.AtlasState;

public class SearchCommands {
    

    public static void handleSearchByName(AtlasState state, String name) {

        if (state.atlasIndex == null) {
            System.out.println("No index available.\n");
            return;
        }

        var results = state.atlasIndex.searchByName(name);

        if (results.isEmpty()) {
            System.out.println("No matching files found: " + name + "\n");
            return;
        }

        System.out.println("\nFound " + results.size() + " matches:\n");

        for (var node : results) {

            System.out.println(
                "Name: " + node.getName()
                + " | ID: " + node.getId()
                + " | Parent ID: " + node.getParentId()
                + " | Type: " + node.getType()
                + "\n"
            );
        }

        System.out.println();
    }


    public static void handleChangeFilters(AtlasState state) {
        System.out.println("dummy change filters executed...\n");
    }


}
