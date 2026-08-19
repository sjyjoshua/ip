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
        while (true) {
            String command = scanner.nextLine();
            System.out.println(separator);

            if (command.equals("bye") || command.equals("BYE") || command.equals("Bye")) {
                System.out.println("Goodbye! Please take me down soon hehe!");
                System.out.println(separator);
                break;
            }

            System.out.println(command);
            System.out.println(separator);
        }
    }
}
