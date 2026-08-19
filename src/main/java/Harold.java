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
        String[] inputs = new String[100];
        boolean[] done = new boolean[100];
        int i = 0;
        while (true) {
            String command = scanner.nextLine();
            System.out.println(separator);

            if (isByeCommand(command)) {
                System.out.println("Goodbye! Please take me down soon hehe!");
                System.out.println(separator);
                break;
            } else if (command.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int k = 0; k < i; k++) {
                    String status = done[k] ? "X" : " ";
                    System.out.printf("%d.[%s] %s%n", k + 1, status, inputs[k]);
                }
            } else if (command.equals("mark")) {
                System.out.println("Please enter a valid task number after mark.");
            } else if (command.startsWith("mark ")) {
                try {
                    int index = Integer.parseInt(command.substring(5)) - 1;
                    if (index < 0 || index >= i) {
                        System.out.println("That task number does not exist.");
                    } else {
                        done[index] = true;
                        System.out.println("Nice! I've marked this task as done:");
                        System.out.printf("  [X] %s%n", inputs[index]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid task number after mark.");
                }
            } else if (command.equals("unmark")) {
                System.out.println("Please enter a valid task number after unmark.");
            } else if (command.startsWith("unmark ")) {
                try {
                    int index = Integer.parseInt(command.substring(7)) - 1;
                    if (index < 0 || index >= i) {
                        System.out.println("That task number does not exist.");
                    } else {
                        done[index] = false;
                        System.out.println("OK, I've marked this task as not done yet:");
                        System.out.printf("  [ ] %s%n", inputs[index]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid task number after unmark.");
                }
            } else {
                inputs[i++] = command;
                System.out.println("added: " + inputs[i - 1]);
            }

            System.out.println(separator);
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
