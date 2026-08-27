import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Harold {
    public static void main(String[] args) {
        Ui ui = new Ui();
        Storage storage = new Storage(Path.of("data", "harold.txt"));
        Task[] tasks = new Task[100];
        int taskCount = 0;
        String loadMessage = null;
        try {
            Storage.LoadResult loadResult = storage.load();
            List<Task> loadedTasks = loadResult.tasks();
            taskCount = Math.min(loadedTasks.size(), tasks.length);
            for (int i = 0; i < taskCount; i++) {
                tasks[i] = loadedTasks.get(i);
            }
            int skippedTaskCount = loadResult.skippedLineCount()
                    + Math.max(0, loadedTasks.size() - tasks.length);
            if (skippedTaskCount > 0) {
                loadMessage = "I skipped " + skippedTaskCount
                        + " invalid task record(s) while loading your data.";
            }
        } catch (IOException e) {
            loadMessage = "I couldn't load your saved tasks, so I started with an empty list.";
        }

        ui.showWelcome(loadMessage);

        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            ui.showSeparator();

            try {
                if (command.isBlank()) {
                    throw new HaroldException("Please enter a command pls.");
                }

                CommandType commandType = getCommandType(command);
                switch (commandType) {
                case BYE -> {
                    ui.showGoodbye();
                    return;
                }
                case LIST -> {
                    if (!command.equals("list")) {
                        throw new HaroldException("The list command does not accept extra text.");
                    }
                    ui.showTaskList(tasks, taskCount);
                }
                case MARK -> {
                    int index = parseTaskIndex(command, "mark", taskCount);
                    tasks[index].markAsDone();
                    storage.save(tasks, taskCount);
                    ui.showTaskMarked(tasks[index]);
                }
                case UNMARK -> {
                    int index = parseTaskIndex(command, "unmark", taskCount);
                    tasks[index].markAsNotDone();
                    storage.save(tasks, taskCount);
                    ui.showTaskUnmarked(tasks[index]);
                }
                case DELETE -> {
                    int index = parseTaskIndex(command, "delete", taskCount);
                    Task removedTask = tasks[index];
                    for (int k = index; k < taskCount - 1; k++) {
                        tasks[k] = tasks[k + 1];
                    }
                    tasks[taskCount - 1] = null;
                    taskCount--;
                    storage.save(tasks, taskCount);
                    ui.showTaskDeleted(removedTask, taskCount);
                }
                case TODO -> {
                    String description = command.length() > 4 ? command.substring(5).trim() : "";
                    requireDescription(description, "todo");
                    requireSpaceInTaskList(taskCount, tasks.length);
                    tasks[taskCount++] = new Todo(description);
                    storage.save(tasks, taskCount);
                    ui.showTaskAdded(tasks[taskCount - 1], taskCount);
                }
                case DEADLINE -> {
                    int byIndex = command.indexOf(" /by");
                    if (byIndex < 0 || byIndex + 4 < command.length()
                            && !Character.isWhitespace(command.charAt(byIndex + 4))) {
                        throw new HaroldException(
                                "A deadline needs '/by <date or time>'. "
                                        + "Try: deadline <description> /by <date or time>");
                    }
                    String description = byIndex < 9 ? "" : command.substring(9, byIndex).trim();
                    String byText = command.substring(byIndex + 4).trim();
                    requireDescription(description, "deadline");
                    if (byText.isEmpty()) {
                        throw new HaroldException("Please enter a date after /by.");
                    }
                    LocalDate by = parseDate(byText, "/by");
                    requireSpaceInTaskList(taskCount, tasks.length);
                    tasks[taskCount++] = new Deadline(description, by);
                    storage.save(tasks, taskCount);
                    ui.showTaskAdded(tasks[taskCount - 1], taskCount);
                }
                case EVENT -> {
                    int fromIndex = command.indexOf(" /from");
                    if (fromIndex < 0 || fromIndex + 6 < command.length()
                            && !Character.isWhitespace(command.charAt(fromIndex + 6))) {
                        throw new HaroldException(
                                "An event needs '/from <start>'. "
                                        + "Try: event <description> /from <start> /to <end>");
                    }
                    int toIndex = command.indexOf(" /to", fromIndex + 6);
                    if (toIndex < 0 || toIndex + 4 < command.length()
                            && !Character.isWhitespace(command.charAt(toIndex + 4))) {
                        throw new HaroldException(
                                "An event needs '/to <end>'. "
                                        + "Try: event <description> /from <start> /to <end>");
                    }
                    String description = fromIndex < 6 ? "" : command.substring(6, fromIndex).trim();
                    String fromText = command.substring(fromIndex + 6, toIndex).trim();
                    String toText = command.substring(toIndex + 4).trim();
                    requireDescription(description, "event");
                    if (fromText.isEmpty()) {
                        throw new HaroldException("Please enter a start date after /from.");
                    } else if (toText.isEmpty()) {
                        throw new HaroldException("Please enter an end date after /to.");
                    }
                    LocalDate from = parseDate(fromText, "/from");
                    LocalDate to = parseDate(toText, "/to");
                    requireSpaceInTaskList(taskCount, tasks.length);
                    tasks[taskCount++] = new Event(description, from, to);
                    storage.save(tasks, taskCount);
                    ui.showTaskAdded(tasks[taskCount - 1], taskCount);
                }
                case UNKNOWN ->
                    throw new HaroldException(
                            "I don't know what '" + command + "' means. "
                                    + "Try todo, deadline, event, list, mark, unmark, delete, or bye.");
                }
            } catch (HaroldException e) {
                ui.showError(e.getMessage());
            } catch (IOException e) {
                ui.showError("I couldn't save your tasks. Please try again.");
            }

            ui.showSeparator();
        }
    }

    /**
     * Returns the type of command entered by the user.
     */
    private static CommandType getCommandType(String command) {
        return isByeCommand(command) ? CommandType.BYE : CommandType.fromCommand(command);
    }

    /**
     * Parses and validates the one-based task number supplied to a task command.
     */
    private static int parseTaskIndex(String command, String commandWord, int taskCount)
            throws HaroldException {
        String numberText = command.substring(commandWord.length()).trim();
        if (numberText.isEmpty()) {
            throw new HaroldException("Please enter a task number after " + commandWord + ".");
        }
        if (taskCount == 0) {
            throw new HaroldException("Your task list is empty, so there is nothing to "
                    + commandWord + ".");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(numberText);
        } catch (NumberFormatException e) {
            throw new HaroldException(
                    "'" + numberText + "' is not a valid task number. "
                            + "Try: " + commandWord + " <task number>");
        }
        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new HaroldException(
                    "Task " + taskNumber + " does not exist. "
                            + "Choose a number from 1 to " + taskCount + ".");
        }
        return taskNumber - 1;
    }

    /**
     * Ensures that a task command contains a description.
     */
    private static void requireDescription(String description, String taskType)
            throws HaroldException {
        if (description.isEmpty()) {
            String article = taskType.equals("event") ? "an" : "a";
            throw new HaroldException(
                    "The description of " + article + " " + taskType + " cannot be empty. "
                            + "Try: " + taskType + " <description>");
        }
    }

    /**
     * Parses a task date and reports the required input format when parsing fails.
     */
    private static LocalDate parseDate(String dateText, String commandMarker)
            throws HaroldException {
        try {
            return TaskDate.parse(dateText);
        } catch (DateTimeParseException e) {
            throw new HaroldException(
                    "Please enter the date after " + commandMarker
                            + " as yyyy-MM-dd, for example 2019-10-15."
            );
        }
    }

    /**
     * Ensures that another task can be stored in the fixed-size task array.
     */
    private static void requireSpaceInTaskList(int taskCount, int capacity)
            throws HaroldException {
        if (taskCount >= capacity) {
            throw new HaroldException("Your task list is full. Complete some tasks before adding more.");
        }
    }

    /**
     * Returns true when the command contains only the letters in "bye",
     * regardless of their order, capitalization, or number of repetitions.
     * Each of the three letters must occur at least once.
     */
    private static boolean isByeCommand(String command) {
        boolean hasB = false;
        boolean hasY = false;
        boolean hasE = false;

        for (int i = 0; i < command.length(); i++) {
            char character = Character.toLowerCase(command.charAt(i));
            switch (character) {
            case 'b':
                hasB = true;
                break;
            case 'y':
                hasY = true;
                break;
            case 'e':
                hasE = true;
                break;
            default:
                return false;
            }
        }

        return hasB && hasY && hasE;
    }
}
