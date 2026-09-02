package harold;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import harold.command.CommandType;
import harold.storage.Storage;
import harold.task.Deadline;
import harold.task.Event;
import harold.task.Task;
import harold.task.TaskDate;
import harold.task.TaskList;
import harold.task.Todo;
import harold.ui.Ui;

/**
 * Coordinates Harold's command handling and task storage independently of the user interface.
 */
public class Harold {
    private static final Path DEFAULT_FILE_PATH = Path.of("data", "harold.txt");
    private static final int MAX_SIMILAR_MATCHES = 3;

    private final Storage storage;
    private final TaskList tasks;
    private final String loadMessage;

    /**
     * Creates Harold using the default task data file.
     */
    public Harold() {
        this(DEFAULT_FILE_PATH);
    }

    /**
     * Creates Harold using the given task data file.
     *
     * @param filePath Path from which tasks are loaded and to which changes are saved.
     */
    public Harold(Path filePath) {
        storage = new Storage(filePath);

        TaskList loadedTasks;
        String loadingWarning = null;
        try {
            Storage.LoadResult loadResult = storage.load();
            loadedTasks = new TaskList(loadResult.tasks());
            int skippedTaskCount = loadResult.skippedLineCount()
                    + loadedTasks.getDiscardedTaskCount();
            if (skippedTaskCount > 0) {
                loadingWarning = "I skipped " + skippedTaskCount
                        + " invalid task record(s) while loading your data.";
            }
        } catch (IOException e) {
            loadingWarning = "I couldn't load your saved tasks, so I started with an empty list.";
            loadedTasks = new TaskList();
        }
        tasks = loadedTasks;
        loadMessage = loadingWarning;
    }

    /**
     * Runs Harold using the original command-line interface.
     *
     * @param args Command-line arguments, which Harold does not currently use.
     */
    public static void main(String[] args) {
        Harold harold = new Harold();
        Ui ui = new Ui();
        ui.showWelcome(harold.getLoadMessage());

        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            ui.showSeparator();

            CommandResult result = harold.respond(command);
            ui.showMessage(result.message());
            ui.showSeparator();
            if (result.isExit()) {
                return;
            }
        }
    }

    /**
     * Returns a warning encountered while loading tasks, if any.
     *
     * @return Loading warning, or {@code null} when loading succeeded.
     */
    public String getLoadMessage() {
        return loadMessage;
    }

    /**
     * Executes one user command and returns the response for display by any interface.
     *
     * @param command Full command entered by the user.
     * @return Response message and whether the interface should stop accepting commands.
     */
    public CommandResult respond(String command) {
        try {
            return executeCommand(command);
        } catch (HaroldException e) {
            return CommandResult.continueWith("OOPS!!! " + e.getMessage());
        } catch (IOException e) {
            return CommandResult.continueWith("OOPS!!! I couldn't save your tasks. Please try again.");
        }
    }

    /**
     * Executes one validated command and persists task-list changes.
     */
    private CommandResult executeCommand(String command) throws HaroldException, IOException {
        if (command.isBlank()) {
            throw new HaroldException("Please enter a command pls.");
        }

        CommandType commandType = getCommandType(command);
        return switch (commandType) {
            case BYE -> CommandResult.exitWith("Goodbye! Please take me down soon hehe!");
            case LIST -> executeList(command);
            case MARK -> executeMark(command);
            case UNMARK -> executeUnmark(command);
            case DELETE -> executeDelete(command);
            case TODO -> executeTodo(command);
            case DEADLINE -> executeDeadline(command);
            case EVENT -> executeEvent(command);
            case FIND -> executeFind(command);
            case UNKNOWN -> throw new HaroldException(
                    "I don't know what '" + command + "' means. "
                            + "Try todo, deadline, event, list, find, mark, unmark, delete, or bye.");
            default -> throw new AssertionError("Unhandled command type: " + commandType);
        };
    }

    /**
     * Validates and executes a list command.
     */
    private CommandResult executeList(String command) throws HaroldException {
        if (!command.equals("list")) {
            throw new HaroldException("The list command does not accept extra text.");
        }
        return CommandResult.continueWith(formatTasks("Here are the tasks in your list:", tasks));
    }

    /**
     * Marks the requested task as complete.
     */
    private CommandResult executeMark(String command) throws HaroldException, IOException {
        int index = parseTaskIndex(command, "mark", tasks.size());
        Task task = tasks.mark(index);
        storage.save(tasks);
        return CommandResult.continueWith("Nice! I've marked this task as done:\n  " + task);
    }

    /**
     * Marks the requested task as incomplete.
     */
    private CommandResult executeUnmark(String command) throws HaroldException, IOException {
        int index = parseTaskIndex(command, "unmark", tasks.size());
        Task task = tasks.unmark(index);
        storage.save(tasks);
        return CommandResult.continueWith("OK, I've marked this task as not done yet:\n  " + task);
    }

    /**
     * Deletes the requested task.
     */
    private CommandResult executeDelete(String command) throws HaroldException, IOException {
        int index = parseTaskIndex(command, "delete", tasks.size());
        Task removedTask = tasks.delete(index);
        storage.save(tasks);
        return CommandResult.continueWith(
                "DELETED. I've removed this task:\n  " + removedTask + "\n" + formatTaskCount());
    }

    /**
     * Creates and stores a todo task.
     */
    private CommandResult executeTodo(String command) throws HaroldException, IOException {
        String description = command.length() > 4 ? command.substring(5).trim() : "";
        requireDescription(description, "todo");
        return addTask(new Todo(description));
    }

    /**
     * Creates and stores a deadline task.
     */
    private CommandResult executeDeadline(String command) throws HaroldException, IOException {
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
        return addTask(new Deadline(description, by));
    }

    /**
     * Creates and stores an event task.
     */
    private CommandResult executeEvent(String command) throws HaroldException, IOException {
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
        return addTask(new Event(description, from, to));
    }

    /**
     * Finds tasks whose descriptions contain the requested keyword.
     */
    private CommandResult executeFind(String command) throws HaroldException {
        String keyword = command.length() > 4 ? command.substring(5).trim() : "";
        if (keyword.isEmpty()) {
            throw new HaroldException("Please enter a keyword after find. Try: find <keyword>");
        }

        List<Task> exactMatches = tasks.find(keyword);
        if (!exactMatches.isEmpty()) {
            return CommandResult.continueWith(
                    formatTasks("Here are the matching tasks in your list:", exactMatches));
        }

        List<Task> similarMatches = tasks.findSimilar(keyword, MAX_SIMILAR_MATCHES);
        if (!similarMatches.isEmpty()) {
            return CommandResult.continueWith(
                    formatTasks(
                            "I couldn't find an exact match. Here are the most similar tasks:",
                            similarMatches));
        }
        return CommandResult.continueWith(
                "I couldn't find any tasks matching '" + keyword + "'.");
    }

    /**
     * Adds and persists one task, then formats its confirmation message.
     */
    private CommandResult addTask(Task task) throws HaroldException, IOException {
        tasks.add(task);
        storage.save(tasks);
        return CommandResult.continueWith(
                "Got it. I've added this task:\n  " + task + "\n" + formatTaskCount());
    }

    /**
     * Formats a heading followed by a numbered task list.
     */
    private static String formatTasks(String heading, TaskList tasks) {
        StringBuilder response = new StringBuilder(heading);
        for (int i = 0; i < tasks.size(); i++) {
            response.append(System.lineSeparator())
                    .append(i + 1)
                    .append('.')
                    .append(tasks.get(i));
        }
        return response.toString();
    }

    /**
     * Formats a heading followed by a numbered task collection.
     */
    private static String formatTasks(String heading, List<Task> tasks) {
        StringBuilder response = new StringBuilder(heading);
        for (int i = 0; i < tasks.size(); i++) {
            response.append(System.lineSeparator())
                    .append(i + 1)
                    .append('.')
                    .append(tasks.get(i));
        }
        return response.toString();
    }

    /**
     * Formats the current task count using the correct singular or plural noun.
     */
    private String formatTaskCount() {
        String taskWord = tasks.size() == 1 ? "task" : "tasks";
        return "Now you have " + tasks.size() + " " + taskWord + " in the list.";
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
