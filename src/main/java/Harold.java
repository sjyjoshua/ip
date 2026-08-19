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
        int i = 0;
        while (true) {
            String command = scanner.nextLine();
            System.out.println(separator);

            if (isByeCommand(command)) {
                System.out.println("Goodbye! Please take me down soon hehe!");
                System.out.println(separator);
                break;
            } else if (command.equals("list")) {
                for (int k = 0; k < i; k++) {
                    System.out.printf("%d. %s%n", k + 1, inputs[k]);
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
