/**
 * Identifies the commands supported by Harold.
 */
public enum CommandType {
    BYE,
    LIST,
    MARK,
    UNMARK,
    DELETE,
    TODO,
    DEADLINE,
    EVENT,
    UNKNOWN;

    /**
     * Returns the command type represented by the first word of a command.
     */
    public static CommandType fromCommand(String command) {
        String commandWord = command.split("\\s+", 2)[0];
        return switch (commandWord) {
        case "list" -> LIST;
        case "mark" -> MARK;
        case "unmark" -> UNMARK;
        case "delete" -> DELETE;
        case "todo" -> TODO;
        case "deadline" -> DEADLINE;
        case "event" -> EVENT;
        default -> UNKNOWN;
        };
    }
}
