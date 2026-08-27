import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Parses and formats dates used by tasks.
 */
public final class TaskDate {
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);

    private TaskDate() {
    }

    /**
     * Parses a date in the ISO yyyy-MM-dd format.
     */
    public static LocalDate parse(String dateText) {
        return LocalDate.parse(dateText, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Formats a date for display to the user.
     */
    public static String format(LocalDate date) {
        return date.format(DISPLAY_FORMATTER);
    }
}
