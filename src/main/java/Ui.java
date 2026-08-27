import java.util.Scanner;

/**
 * Handles console input and output for Harold.
 */
public class Ui {
    private static final String SEPARATOR =
            "____________________________________________________________";
    private static final String TAG = " _   _                 _     _ \n"
            + "| | | | __ _ _ __ ___ | | __| |\n"
            + "| |_| |/ _` | '__/ _ \\| |/ _` |\n"
            + "|  _  | (_| | | | (_) | | (_| |\n"
            + "|_| |_|\\__,_|_|  \\___/|_|\\__,_|\n";
    private static final String BANNER = "           / \\__ /\\\n"
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
            + TAG;

    private final Scanner scanner;

    /**
     * Creates a console UI that reads from standard input.
     */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /**
     * Displays the greeting and an optional message about loading task data.
     */
    public void showWelcome(String loadMessage) {
        System.out.println(SEPARATOR);
        System.out.print(BANNER);
        System.out.println("Arf Arf, I mean WOOF WOOF! I'm Harold.");
        System.out.println("What can I do for you, besides eat my poopoo?");
        if (loadMessage != null) {
            showError(loadMessage);
        }
        System.out.println(SEPARATOR);
    }

    /**
     * Returns whether another command is available from the user.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads and returns the next command from the user.
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Displays the separator used between command responses.
     */
    public void showSeparator() {
        System.out.println(SEPARATOR);
    }

    /**
     * Displays Harold's goodbye message and a closing separator.
     */
    public void showGoodbye() {
        System.out.println("Goodbye! Please take me down soon hehe!");
        showSeparator();
    }

    /**
     * Displays all tasks in their current order.
     */
    public void showTaskList(Task[] tasks, int taskCount) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.printf("%d.%s%n", i + 1, tasks[i]);
        }
    }

    /**
     * Displays confirmation that a task was marked complete.
     */
    public void showTaskMarked(Task task) {
        System.out.println("Nice! I've marked this task as done:");
        System.out.printf("  %s%n", task);
    }

    /**
     * Displays confirmation that a task was marked incomplete.
     */
    public void showTaskUnmarked(Task task) {
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.printf("  %s%n", task);
    }

    /**
     * Displays confirmation that a task was added and the new task count.
     */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println("Got it. I've added this task:");
        System.out.printf("  %s%n", task);
        showTaskCount(taskCount);
    }

    /**
     * Displays confirmation that a task was deleted and the new task count.
     */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println("DELETED. I've removed this task:");
        System.out.printf("  %s%n", task);
        showTaskCount(taskCount);
    }

    /**
     * Displays an error message using Harold's error prefix.
     */
    public void showError(String message) {
        System.out.println("OOPS!!! " + message);
    }

    private static void showTaskCount(int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.printf("Now you have %d %s in the list.%n", taskCount, taskWord);
    }
}
