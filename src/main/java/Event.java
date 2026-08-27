/**
 * Represents a task with a specified start and end date or time.
 */
public class Event extends Task {
    private final String from;
    private final String to;

    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event start text.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the event end text.
     */
    public String getTo() {
        return to;
    }

    @Override
    protected String getTypeIcon() {
        return "E";
    }

    @Override
    public String toString() {
        return String.format("%s (from: %s to: %s)", super.toString(), from, to);
    }
}
