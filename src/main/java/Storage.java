import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Loads and saves Harold's tasks using an OS-independent relative path.
 */
public class Storage {
    private static final String FIELD_SEPARATOR = "\t";

    private final Path filePath;

    /**
     * Creates storage backed by the given file path.
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads all valid task records, skipping malformed records without failing startup.
     */
    public LoadResult load() throws IOException {
        List<Task> tasks = new ArrayList<>();
        int skippedLineCount = 0;

        if (!Files.exists(filePath)) {
            return new LoadResult(tasks, skippedLineCount);
        }

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            try {
                tasks.add(parseTask(line));
            } catch (IllegalArgumentException e) {
                skippedLineCount++;
            }
        }
        return new LoadResult(tasks, skippedLineCount);
    }

    /**
     * Saves the current tasks, creating the parent directory when necessary.
     */
    public void save(TaskList tasks) throws IOException {
        Path parentDirectory = filePath.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        List<String> lines = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            lines.add(formatTask(tasks.get(i)));
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    private static Task parseTask(String line) {
        String[] fields = line.split(FIELD_SEPARATOR, -1);
        if (fields.length < 3) {
            throw new IllegalArgumentException("Task record has too few fields");
        }

        boolean isDone;
        if (fields[1].equals("1")) {
            isDone = true;
        } else if (fields[1].equals("0")) {
            isDone = false;
        } else {
            throw new IllegalArgumentException("Task status must be 0 or 1");
        }

        String description = decode(fields[2]);
        Task task = switch (fields[0]) {
        case "T" -> {
            requireFieldCount(fields, 3);
            yield new Todo(description);
        }
        case "D" -> {
            requireFieldCount(fields, 4);
            yield new Deadline(description, TaskDate.parse(decode(fields[3])));
        }
        case "E" -> {
            requireFieldCount(fields, 5);
            yield new Event(
                    description,
                    TaskDate.parse(decode(fields[3])),
                    TaskDate.parse(decode(fields[4]))
            );
        }
        default -> throw new IllegalArgumentException("Unknown task type");
        };

        if (description.isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be empty");
        }
        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    private static String formatTask(Task task) {
        String status = task.isDone() ? "1" : "0";
        String commonFields = task.getTypeIcon() + FIELD_SEPARATOR + status
                + FIELD_SEPARATOR + encode(task.getDescription());

        if (task instanceof Deadline deadline) {
            return commonFields + FIELD_SEPARATOR + encode(deadline.getBy().toString());
        } else if (task instanceof Event event) {
            return commonFields + FIELD_SEPARATOR + encode(event.getFrom().toString())
                    + FIELD_SEPARATOR + encode(event.getTo().toString());
        }
        return commonFields;
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private static void requireFieldCount(String[] fields, int expectedCount) {
        if (fields.length != expectedCount) {
            throw new IllegalArgumentException("Unexpected number of task fields");
        }
    }

    /**
     * Contains successfully loaded tasks and the number of malformed records skipped.
     */
    public record LoadResult(List<Task> tasks, int skippedLineCount) {
    }
}
