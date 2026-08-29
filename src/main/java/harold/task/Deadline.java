package harold.task;

import java.time.LocalDate;

/**
 * Represents a task that must be completed by a specified date.
 */
public class Deadline extends Task {
    private final LocalDate by;

    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline date.
     */
    public LocalDate getBy() {
        return by;
    }

    @Override
    public String getTypeIcon() {
        return "D";
    }

    @Override
    public String toString() {
        return String.format("%s (by: %s)", super.toString(), TaskDate.format(by));
    }
}
