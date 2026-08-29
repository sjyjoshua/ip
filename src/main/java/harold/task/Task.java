package harold.task;

/**
 * Represents a task that can be marked as done or not done.
 */
public abstract class Task {
    private final String description;
    private boolean isDone;

    /**
     * Creates an incomplete task with the given description.
     */
    protected Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    public void markAsDone() {
        isDone = true;
    }

    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Returns the task description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether the task is complete.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns the icon that identifies the task type.
     */
    public abstract String getTypeIcon();

    @Override
    public String toString() {
        String statusIcon = isDone ? "X" : " ";
        return String.format("[%s][%s] %s", getTypeIcon(), statusIcon, description);
    }
}
