package harold.task;

import java.time.LocalDate;

/**
 * Represents a task with specified start and end dates.
 */
public class Event extends Task {
    private final LocalDate from;
    private final LocalDate to;

    /**
     * Creates an incomplete event with the given description and date range.
     *
     * @param description Description of the event.
     * @param from Event start date.
     * @param to Event end date.
     */
    public Event(String description, LocalDate from, LocalDate to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event start date.
     *
     * @return Event start date.
     */
    public LocalDate getFrom() {
        return from;
    }

    /**
     * Returns the event end date.
     *
     * @return Event end date.
     */
    public LocalDate getTo() {
        return to;
    }

    /**
     * Returns the icon used to identify events.
     *
     * @return Event type icon.
     */
    @Override
    public String getTypeIcon() {
        return "E";
    }

    /**
     * Returns the event formatted with its status, description, and date range.
     *
     * @return User-facing event representation.
     */
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
