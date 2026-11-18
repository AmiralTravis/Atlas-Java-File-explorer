
package atlas.search;

import java.time.LocalDate;
import java.util.Set;

public class FilterState {

    public LocalDate startDate;
    public LocalDate endDate;

    public long minSize;
    public long maxSize;

    public Set<String> extensions = Set.of();

    public String toDisplayString() {
        return """
                date       : %s - %s
                size       : %dB - %dB
                extensions : %s
                """.formatted(
                    startDate != null ? startDate : "Any",
                    endDate != null ? endDate : "Any",
                    minSize,
                    maxSize,
                    extensions.isEmpty()
                        ? "All"
                        : String.join(", ", extensions)
                );
    }
}
