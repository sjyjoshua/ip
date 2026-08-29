package harold;

/**
 * Represents an invalid command or task operation in Harold.
 */
public class HaroldException extends Exception {
    /**
     * Creates an exception with a message suitable for display to the user.
     *
     * @param message Explanation of the invalid command or task operation.
     */
    public HaroldException(String message) {
        super(message);
    }
}
