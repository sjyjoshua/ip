package harold.task;

import java.time.LocalDate;

/**
 * Represents a task with specified start and end dates.
 */
public class Event extends Task {
    private final LocalDate from;
    private final LocalDate to;

    public Event(String description, LocalDate from, LocalDate to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event start date.
     */
    public LocalDate getFrom() {
        return from;
    }

    /**
     * Returns the event end date.
     */
    public LocalDate getTo() {
        return to;
    }

    @Override
    public String getTypeIcon() {
        return "E";
    }

    @Override
    public String toString() {
        return String.format(
                "%s (from: %s to: %s)",
                super.toString(),
                TaskDate.format(from),
                TaskDate.format(to)
        );
    }
}
