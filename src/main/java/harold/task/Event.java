package harold.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a task with specified start and end dates.
 */
public class Event extends Task {
    private final TaskDateTime from;
    private final TaskDateTime to;

    /**
     * Creates an incomplete event with the given description and date range.
     *
     * @param description Description of the event.
     * @param from Event start date.
     * @param to Event end date.
     */
    public Event(String description, LocalDate from, LocalDate to) {
        this(description, TaskDateTime.dateOnly(from), TaskDateTime.dateOnly(to));
    }

    /**
     * Creates an incomplete event with the given description and timed range.
     *
     * @param description Description of the event.
     * @param from Event start date and time.
     * @param to Event end date and time.
     */
    public Event(String description, LocalDateTime from, LocalDateTime to) {
        this(description, TaskDateTime.timed(from), TaskDateTime.timed(to));
    }

    /**
     * Creates an incomplete event using endpoints of matching precision.
     *
     * @param description Description of the event.
     * @param from Event start endpoint.
     * @param to Event end endpoint.
     * @throws IllegalArgumentException If precisions differ or the range is invalid.
     */
    public Event(String description, TaskDateTime from, TaskDateTime to) {
        super(description);
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        validateRange();
    }

    /**
     * Returns the event start date.
     *
     * @return Event start date.
     */
    public TaskDateTime getFrom() {
        return from;
    }

    /**
     * Returns the event end date.
     *
     * @return Event end date.
     */
    public TaskDateTime getTo() {
        return to;
    }

    /**
     * Returns whether this event has precise start and end times.
     *
     * @return {@code true} when both endpoints include times.
     */
    public boolean hasTime() {
        return from.hasTime();
    }

    /**
     * Returns whether this timed event overlaps another timed event.
     *
     * @param other Event against which to check.
     * @return {@code true} when both events are timed and their intervals overlap.
     */
    public boolean clashesWith(Event other) {
        if (!hasTime() || !other.hasTime()) {
            return false;
        }
        return from.toLocalDateTime().isBefore(other.to.toLocalDateTime())
                && other.from.toLocalDateTime().isBefore(to.toLocalDateTime());
    }

    /**
     * Returns whether this event and another occupy at least one common calendar date.
     *
     * @param other Event against which to check.
     * @return {@code true} when the event date ranges intersect.
     */
    public boolean sharesDateWith(Event other) {
        return !getLastOccupiedDate().isBefore(other.from.toLocalDate())
                && !other.getLastOccupiedDate().isBefore(from.toLocalDate());
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
                from.format(),
                to.format()
        );
    }

    /**
     * Ensures that both endpoints have matching precision and form a valid interval.
     */
    private void validateRange() {
        if (from.hasTime() != to.hasTime()) {
            throw new IllegalArgumentException("Event endpoints must use matching precision");
        }
        if (hasTime() && !to.toLocalDateTime().isAfter(from.toLocalDateTime())) {
            throw new IllegalArgumentException("Timed event must end after it starts");
        }
        if (!hasTime() && to.toLocalDate().isBefore(from.toLocalDate())) {
            throw new IllegalArgumentException("Date-only event cannot end before it starts");
        }
    }

    /**
     * Returns the last occupied date while respecting a timed interval's exclusive end.
     */
    private LocalDate getLastOccupiedDate() {
        if (hasTime()) {
            return to.toLocalDateTime().minusNanos(1).toLocalDate();
        }
        return to.toLocalDate();
    }
}
