package atlas;

public interface QueueItem {}

class CommandItem implements QueueItem {

    String value;

    CommandItem(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }
}
