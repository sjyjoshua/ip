import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Harold {
    public static void main(String[] args) {
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

        String separator = "____________________________________________________________";
        String tag = " _   _                 _     _ \n"
                + "| | | | __ _ _ __ ___ | | __| |\n"
                + "| |_| |/ _` | '__/ _ \\| |/ _` |\n"
                + "|  _  | (_| | | | (_) | | (_| |\n"
                + "|_| |_|\\__,_|_|  \\___/|_|\\__,_|\n";
        String banner = "           / \\__ /\\\n"
                + "          /  _  _  \\\n"
                + "         |  / \\/ \\  |\n"
                + "         |  \\_/\\_/  |\n"
                + "         |    __    |\n"
                + "          \\  (__)  /\n"
                + "           \\______/\n"
                + "           /|    |\\\n"
                + "          / |____| \\\n"
                + "            / || \\\n"
                + "           (_/  \\_)\\\n"
                + tag;

        System.out.println(separator);
        System.out.print(banner);
        System.out.println("Arf Arf, I mean WOOF WOOF! I'm Harold.");
        System.out.println("What can I do for you, besides eat my poopoo?");
        if (loadMessage != null) {
            System.out.println("OOPS!!! " + loadMessage);
        }
        System.out.println(separator);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(separator);

            try {
                if (command.isBlank()) {
                    throw new HaroldException("Please enter a command pls.");
                }

                CommandType commandType = getCommandType(command);
                switch (commandType) {
                case BYE -> {
                    System.out.println("Goodbye! Please take me down soon hehe!");
                    System.out.println(separator);
                    return;
                }
                case LIST -> {
                    if (!command.equals("list")) {
                        throw new HaroldException("The list command does not accept extra text.");
                    }
                    System.out.println("Here are the tasks in your list:");
                    for (int k = 0; k < taskCount; k++) {
                        System.out.printf("%d.%s%n", k + 1, tasks[k]);
                    }
                }
                case MARK -> {
                    int index = parseTaskIndex(command, "mark", taskCount);
                    tasks[index].markAsDone();
                    storage.save(tasks, taskCount);
                    System.out.println("Nice! I've marked this task as done:");
                    System.out.printf("  %s%n", tasks[index]);
                }
                case UNMARK -> {
                    int index = parseTaskIndex(command, "unmark", taskCount);
                    tasks[index].markAsNotDone();
                    storage.save(tasks, taskCount);
                    System.out.println("OK, I've marked this task as not done yet:");
                    System.out.printf("  %s%n", tasks[index]);
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
                    System.out.println("DELETED. I've removed this task:");
                    System.out.printf("  %s%n", removedTask);

                    String taskWord = taskCount == 1 ? "task" : "tasks";
                    System.out.printf(
                            "Now you have %d %s in the list.%n",
                            taskCount,
                            taskWord
                    );
                }
                case TODO -> {
                    String description = command.length() > 4 ? command.substring(5).trim() : "";
                    requireDescription(description, "todo");
                    requireSpaceInTaskList(taskCount, tasks.length);
                    tasks[taskCount++] = new Todo(description);
                    storage.save(tasks, taskCount);
                    printTaskAdded(tasks[taskCount - 1], taskCount);
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
                    String by = command.substring(byIndex + 4).trim();
                    requireDescription(description, "deadline");
                    if (by.isEmpty()) {
                        throw new HaroldException("Please enter a date or time after /by.");
                    }
                    requireSpaceInTaskList(taskCount, tasks.length);
                    tasks[taskCount++] = new Deadline(description, by);
                    storage.save(tasks, taskCount);
                    printTaskAdded(tasks[taskCount - 1], taskCount);
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
                    String from = command.substring(fromIndex + 6, toIndex).trim();
                    String to = command.substring(toIndex + 4).trim();
                    requireDescription(description, "event");
                    if (from.isEmpty()) {
                        throw new HaroldException("Please enter a start date or time after /from.");
                    } else if (to.isEmpty()) {
                        throw new HaroldException("Please enter an end date or time after /to.");
                    }
                    requireSpaceInTaskList(taskCount, tasks.length);
                    tasks[taskCount++] = new Event(description, from, to);
                    storage.save(tasks, taskCount);
                    printTaskAdded(tasks[taskCount - 1], taskCount);
                }
                case UNKNOWN ->
                    throw new HaroldException(
                            "I don't know what '" + command + "' means. "
                                    + "Try todo, deadline, event, list, mark, unmark, delete, or bye.");
                }
            } catch (HaroldException e) {
                System.out.println("OOPS!!! " + e.getMessage());
            } catch (IOException e) {
                System.out.println("OOPS!!! I couldn't save your tasks. Please try again.");
            }

            System.out.println(separator);
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
     * Ensures that another task can be stored in the fixed-size task array.
     */
    private static void requireSpaceInTaskList(int taskCount, int capacity)
            throws HaroldException {
        if (taskCount >= capacity) {
            throw new HaroldException("Your task list is full. Complete some tasks before adding more.");
        }
    }

    /**
     * Prints confirmation that a task was added and reports the new task count.
     */
    private static void printTaskAdded(Task task, int taskCount) {
        System.out.println("Got it. I've added this task:");
        System.out.printf("  %s%n", task);
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.printf("Now you have %d %s in the list.%n", taskCount, taskWord);
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
