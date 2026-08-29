package harold.task;

import java.util.ArrayList;
import java.util.List;

import harold.HaroldException;

/**
 * Owns Harold's task collection and provides operations that modify it.
 */
public class TaskList {
    private static final int MAX_TASK_COUNT = 100;

    private final List<Task> tasks;
    private final int discardedTaskCount;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this(List.of());
    }

    /**
     * Creates a task list containing up to the supported number of initial tasks.
     *
     * @param initialTasks Tasks with which to initialize the list.
     */
    public TaskList(List<Task> initialTasks) {
        int acceptedTaskCount = Math.min(initialTasks.size(), MAX_TASK_COUNT);
        tasks = new ArrayList<>(initialTasks.subList(0, acceptedTaskCount));
        discardedTaskCount = initialTasks.size() - acceptedTaskCount;
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task Task to add.
     * @throws HaroldException If the task list is full.
     */
    public void add(Task task) throws HaroldException {
        if (tasks.size() >= MAX_TASK_COUNT) {
            throw new HaroldException(
                    "Your task list is full. Complete some tasks before adding more."
            );
        }
        tasks.add(task);
    }

    /**
     * Deletes and returns the task at the specified zero-based index.
     *
     * @param index Zero-based task index.
     * @return Deleted task.
     * @throws IndexOutOfBoundsException If the index does not identify a task.
     */
    public Task delete(int index) {
        return tasks.remove(index);
    }

    /**
     * Marks and returns the task at the specified zero-based index.
     *
     * @param index Zero-based task index.
     * @return Marked task.
     * @throws IndexOutOfBoundsException If the index does not identify a task.
     */
    public Task mark(int index) {
        Task task = tasks.get(index);
        task.markAsDone();
        return task;
    }

    /**
     * Unmarks and returns the task at the specified zero-based index.
     *
     * @param index Zero-based task index.
     * @return Unmarked task.
     * @throws IndexOutOfBoundsException If the index does not identify a task.
     */
    public Task unmark(int index) {
        Task task = tasks.get(index);
        task.markAsNotDone();
        return task;
    }

    /**
     * Returns the task at the specified zero-based index.
     *
     * @param index Zero-based task index.
     * @return Task at the specified index.
     * @throws IndexOutOfBoundsException If the index does not identify a task.
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return Number of tasks.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns the number of initial tasks omitted because the list was full.
     *
     * @return Number of discarded initial tasks.
     */
    public int getDiscardedTaskCount() {
        return discardedTaskCount;
    }
}
