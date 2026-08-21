import java.util.Scanner;

public class Harold {
    public static void main(String[] args) {
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
        System.out.println(separator);

        Scanner scanner = new Scanner(System.in);
        Task[] tasks = new Task[100];
        int taskCount = 0;
        while (true) {
            String command = scanner.nextLine();
            System.out.println(separator);

            if (isByeCommand(command)) {
                System.out.println("Goodbye! Please take me down soon hehe!");
                System.out.println(separator);
                break;
            } else if (command.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int k = 0; k < taskCount; k++) {
                    System.out.printf("%d.%s%n", k + 1, tasks[k]);
                }
            } else if (command.equals("mark")) {
                System.out.println("Please enter a valid task number after mark.");
            } else if (command.startsWith("mark ")) {
                try {
                    int index = Integer.parseInt(command.substring(5)) - 1;
                    if (index < 0 || index >= taskCount) {
                        System.out.println("That task number does not exist.");
                    } else {
                        tasks[index].markAsDone();
                        System.out.println("Nice! I've marked this task as done:");
                        System.out.printf("  %s%n", tasks[index]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid task number after mark.");
                }
            } else if (command.equals("unmark")) {
                System.out.println("Please enter a valid task number after unmark.");
            } else if (command.startsWith("unmark ")) {
                try {
                    int index = Integer.parseInt(command.substring(7)) - 1;
                    if (index < 0 || index >= taskCount) {
                        System.out.println("That task number does not exist.");
                    } else {
                        tasks[index].markAsNotDone();
                        System.out.println("OK, I've marked this task as not done yet:");
                        System.out.printf("  %s%n", tasks[index]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid task number after unmark.");
                }
            } else if (command.equals("todo")) {
                System.out.println("Please enter a description after todo.");
            } else if (command.startsWith("todo ")) {
                String description = command.substring(5).trim();
                if (description.isEmpty()) {
                    System.out.println("Please enter a description after todo.");
                } else if (taskCount >= tasks.length) {
                    System.out.println("Your task list is full.");
                } else {
                    tasks[taskCount++] = new Todo(description);
                    printTaskAdded(tasks[taskCount - 1], taskCount);
                }
            } else if (command.equals("deadline")) {
                System.out.println("Use: deadline <description> /by <date or time>");
            } else if (command.startsWith("deadline ")) {
                int byIndex = command.indexOf(" /by ", 9);
                if (byIndex < 0 || command.substring(9, byIndex).trim().isEmpty()
                        || command.substring(byIndex + 5).trim().isEmpty()) {
                    System.out.println("Use: deadline <description> /by <date or time>");
                } else if (taskCount >= tasks.length) {
                    System.out.println("Your task list is full.");
                } else {
                    String description = command.substring(9, byIndex).trim();
                    String by = command.substring(byIndex + 5).trim();
                    tasks[taskCount++] = new Deadline(description, by);
                    printTaskAdded(tasks[taskCount - 1], taskCount);
                }
            } else if (command.equals("event")) {
                System.out.println("Use: event <description> /from <start> /to <end>");
            } else if (command.startsWith("event ")) {
                int fromIndex = command.indexOf(" /from ", 6);
                int toIndex = fromIndex < 0 ? -1 : command.indexOf(" /to ", fromIndex + 7);
                if (fromIndex < 0 || toIndex < 0
                        || command.substring(6, fromIndex).trim().isEmpty()
                        || command.substring(fromIndex + 7, toIndex).trim().isEmpty()
                        || command.substring(toIndex + 5).trim().isEmpty()) {
                    System.out.println("Use: event <description> /from <start> /to <end>");
                } else if (taskCount >= tasks.length) {
                    System.out.println("Your task list is full.");
                } else {
                    String description = command.substring(6, fromIndex).trim();
                    String from = command.substring(fromIndex + 7, toIndex).trim();
                    String to = command.substring(toIndex + 5).trim();
                    tasks[taskCount++] = new Event(description, from, to);
                    printTaskAdded(tasks[taskCount - 1], taskCount);
                }
            } else {
                System.out.println("I don't understand that command.");
            }

            System.out.println(separator);
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
