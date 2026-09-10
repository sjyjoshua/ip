package harold.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents an event endpoint with either date-only or date-and-time precision.
 */
public final class TaskDateTime {
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HHmm").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d yyyy, h:mm a", Locale.ENGLISH);

    private final LocalDateTime value;
    private final boolean hasTime;

    private TaskDateTime(LocalDateTime value, boolean hasTime) {
        this.value = Objects.requireNonNull(value);
        this.hasTime = hasTime;
    }

    /**
     * Creates a date-only event endpoint.
     *
     * @param date Calendar date represented by the endpoint.
     * @return Date-only endpoint.
     */
    public static TaskDateTime dateOnly(LocalDate date) {
        return new TaskDateTime(date.atStartOfDay(), false);
    }

    /**
     * Creates an event endpoint with date-and-time precision.
     *
     * @param dateTime Date and time represented by the endpoint.
     * @return Timed endpoint.
     */
    public static TaskDateTime timed(LocalDateTime dateTime) {
        return new TaskDateTime(dateTime, true);
    }

    /**
     * Parses a user-entered endpoint in either supported event format.
     *
     * @param text User-entered date or date and time.
     * @return Parsed endpoint.
     * @throws DateTimeParseException If the text does not match a supported format.
     */
    public static TaskDateTime parseInput(String text) {
        if (text.contains(" ")) {
            return timed(LocalDateTime.parse(text, INPUT_DATE_TIME_FORMATTER));
        }
        return dateOnly(TaskDate.parse(text));
    }

    /**
     * Parses an endpoint from Harold's backward-compatible storage representation.
     *
     * @param text Stored ISO date or date-time.
     * @return Parsed endpoint.
     * @throws DateTimeParseException If the stored text is invalid.
     */
    public static TaskDateTime parseStored(String text) {
        if (text.contains("T")) {
            return timed(LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return dateOnly(TaskDate.parse(text));
    }

    /**
     * Returns whether this endpoint includes a time.
     *
     * @return {@code true} when the endpoint has date-and-time precision.
     */
    public boolean hasTime() {
        return hasTime;
    }

    /**
     * Returns the calendar date represented by this endpoint.
     *
     * @return Calendar date.
     */
    public LocalDate toLocalDate() {
        return value.toLocalDate();
    }

    /**
     * Returns the date and time represented internally by this endpoint.
     *
     * @return Date and time, using midnight for a date-only endpoint.
     */
    public LocalDateTime toLocalDateTime() {
        return value;
    }

    /**
     * Formats this endpoint for display.
     *
     * @return Human-readable date or date and time.
     */
    public String format() {
        return hasTime
                ? value.format(DISPLAY_DATE_TIME_FORMATTER)
                : TaskDate.format(value.toLocalDate());
    }

    /**
     * Formats this endpoint for storage without losing its precision.
     *
     * @return ISO date for a date-only endpoint, or ISO date-time for a timed endpoint.
     */
    public String formatForStorage() {
        return hasTime
                ? value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : value.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
