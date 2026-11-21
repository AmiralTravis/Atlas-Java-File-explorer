package atlas.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atlas.scanner.FileSystemNode;
import atlas.scanner.Index;

public class SearchEngine {

    public record Match(FileSystemNode node, double score, boolean exact) {}

    private static final double FUZZY_THRESHOLD = 0.70;
    private static final int MAX_FUZZY_RESULTS = 25;

    public static List<Match> search(Index index, String rawQuery, FilterState filters) {

        List<Match> results = new ArrayList<>();

        String q = rawQuery.trim().toLowerCase();
        if (q.isEmpty()) {
            return results;
        }

        Map<String, Set<Long>> byName = index.getNodeIdsByName();
        Set<Long> seen = new HashSet<>();

        // ---------- exact matches ----------
        Set<Long> candidates = new LinkedHashSet<>();

        Set<Long> a = byName.get(q);
        if (a != null) candidates.addAll(a);

        Set<Long> b = byName.get(NameUtils.baseName(q));
        if (b != null) candidates.addAll(b);

        for (Long id : candidates) {
            FileSystemNode node = index.getNode(id);
            if (node == null) continue;

            // "text" matches text.txt AND text.json; "text.txt" matches only text.txt
            boolean exact = node.getName().equalsIgnoreCase(q)
                || NameUtils.key(node).equals(q);

            if (!exact || !filters.matches(node, index)) continue;

            results.add(new Match(node, 1.0, true));
            seen.add(id);
        }

        // ---------- fuzzy matches ----------
        String fuzzyQuery = NameUtils.baseName(q);
        String queryExt = NameUtils.extension(q);

        List<Match> fuzzy = new ArrayList<>();

        for (Map.Entry<String, Set<Long>> entry : byName.entrySet()) {

            double score = similarity(fuzzyQuery, entry.getKey());
            if (score < FUZZY_THRESHOLD) continue;

            for (Long id : entry.getValue()) {
                if (seen.contains(id)) continue;

                FileSystemNode node = index.getNode(id);
                if (node == null) continue;

                // if user typed an extension, respect it for files
                if (!queryExt.isEmpty()
                    && node.getType() == FileSystemNode.NodeType.FILE
                    && !NameUtils.extension(node.getName()).equals(queryExt)) {
                    continue;
                }

                if (!filters.matches(node, index)) continue;

                fuzzy.add(new Match(node, score, false));
            }
        }

        fuzzy.sort(Comparator.comparingDouble(Match::score).reversed());
        results.addAll(fuzzy.subList(0, Math.min(MAX_FUZZY_RESULTS, fuzzy.size())));

        return results;
    }

    private static double similarity(String q, String candidate) {

        if (q.equals(candidate)) return 1.0;

        // exact substring: "resume" inside "resume_final_v2"
        if (q.length() >= 3 && candidate.contains(q)) {
            return 0.75 + 0.2 * ((double) q.length() / candidate.length());
        }

        double best = 0.0;

        // fuzzy substring: "resuem" ~ "resume" inside "resume_final_v2"
        int allowed = maxEdits(q.length());

        if (allowed > 0) {
            int subDist = bestSubstringDistance(q, candidate);

            if (subDist <= allowed) {
                // stays below exact-substring scores (0.75+), above FUZZY_THRESHOLD
                best = FUZZY_THRESHOLD
                    + 0.04 * (allowed - subDist + 1)
                    + 0.01 * Math.min(1.0, (double) q.length() / candidate.length());
            }
        }

        // whole-string fuzzy (good when query is roughly the entire name)
        int max = Math.max(q.length(), candidate.length());
        if (max == 0) return 1.0;

        // cheap prune: lengths too different to ever reach threshold
        if (Math.abs(q.length() - candidate.length()) <= max * (1 - FUZZY_THRESHOLD)) {
            int dist = levenshtein(q, candidate);
            best = Math.max(best, 1.0 - ((double) dist / max));
        }

        return best;
    }

    // how many typos we tolerate inside a matched substring, by query length
    private static int maxEdits(int queryLength) {
        if (queryLength <= 3) return 0;   // too short, would match everything
        if (queryLength <= 7) return 1;
        return 2;
    }

    // Smallest edit distance between q and ANY substring of text.
    // (Sellers' algorithm: Levenshtein with free start and free end in text,
    //  plus adjacent-letter swaps counted as a single edit.)
    private static int bestSubstringDistance(String q, String text) {
        int m = q.length();
        int n = text.length();

        int[] prev2 = new int[m + 1];
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];

        // before reading any text: matching q[0..i) costs i insertions
        for (int i = 0; i <= m; i++) prev[i] = i;

        int best = prev[m];

        for (int j = 1; j <= n; j++) {

            curr[0] = 0; // free start: the match may begin at any text position

            for (int i = 1; i <= m; i++) {
                int cost = q.charAt(i - 1) == text.charAt(j - 1) ? 0 : 1;

                int v = Math.min(
                    Math.min(curr[i - 1] + 1, prev[i] + 1),
                    prev[i - 1] + cost
                );

                // swapped adjacent letters: "em" vs "me"
                if (i > 1 && j > 1
                    && q.charAt(i - 1) == text.charAt(j - 2)
                    && q.charAt(i - 2) == text.charAt(j - 1)) {
                    v = Math.min(v, prev2[i - 2] + 1);
                }

                curr[i] = v;
            }

            best = Math.min(best, curr[m]); // free end: match may stop anywhere

            int[] tmp = prev2;
            prev2 = prev;
            prev = curr;
            curr = tmp;
        }

        return best;
    }

    private static int levenshtein(String s, String t) {
        int[] prev = new int[t.length() + 1];
        int[] curr = new int[t.length() + 1];

        for (int j = 0; j <= t.length(); j++) prev[j] = j;

        for (int i = 1; i <= s.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= t.length(); j++) {
                int cost = s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(
                    Math.min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                );
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }

        return prev[t.length()];
    }
}