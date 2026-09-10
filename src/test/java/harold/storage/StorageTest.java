package harold.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import harold.HaroldException;
import harold.task.Deadline;
import harold.task.Event;
import harold.task.Task;
import harold.task.TaskList;
import harold.task.Todo;

class StorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void load_missingFile_returnsEmptyResult() throws Exception {
        Storage storage = new Storage(temporaryDirectory.resolve("data/tasks.txt"));

        Storage.LoadResult result = storage.load();

        assertTrue(result.tasks().isEmpty());
        assertEquals(0, result.skippedLineCount());
    }

    @Test
    void saveAndLoad_allTaskTypes_preservesTaskData() throws Exception {
        Path filePath = temporaryDirectory.resolve("data/tasks.txt");
        Storage storage = new Storage(filePath);
        TaskList tasks = createTaskList();

        storage.save(tasks);
        Storage.LoadResult result = storage.load();

        assertEquals(3, result.tasks().size());
        assertEquals(0, result.skippedLineCount());
        assertTodo(result.tasks().get(0));
        assertDeadline(result.tasks().get(1));
        assertEvent(result.tasks().get(2));
    }

    @Test
    void load_corruptedRecord_skipsOnlyCorruptedRecord() throws Exception {
        Path filePath = temporaryDirectory.resolve("data/tasks.txt");
        Storage storage = new Storage(filePath);
        TaskList tasks = new TaskList();
        tasks.add(new Todo("valid task"));
        storage.save(tasks);
        Files.writeString(
                filePath,
                "corrupted task record\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND
        );

        Storage.LoadResult result = storage.load();

        assertEquals(1, result.tasks().size());
        assertEquals("valid task", result.tasks().get(0).getDescription());
        assertEquals(1, result.skippedLineCount());
    }

    @Test
    void save_emptyList_replacesExistingData() throws Exception {
        Storage storage = new Storage(temporaryDirectory.resolve("data/tasks.txt"));
        TaskList tasks = new TaskList();
        tasks.add(new Todo("task to remove"));
        storage.save(tasks);

        storage.save(new TaskList());
        Storage.LoadResult result = storage.load();

        assertTrue(result.tasks().isEmpty());
        assertEquals(0, result.skippedLineCount());
    }

    @Test
    void saveAndLoad_timedEvent_preservesDateTimePrecision() throws Exception {
        Storage storage = new Storage(temporaryDirectory.resolve("data/tasks.txt"));
        Event event = new Event(
                "timed meeting",
                LocalDateTime.of(2026, 9, 10, 14, 30),
                LocalDateTime.of(2026, 9, 10, 15, 45)
        );

        storage.save(new TaskList(event));
        Storage.LoadResult result = storage.load();

        Event loadedEvent = assertInstanceOf(Event.class, result.tasks().get(0));
        assertTrue(loadedEvent.hasTime());
        assertEquals(
                LocalDateTime.of(2026, 9, 10, 14, 30),
                loadedEvent.getFrom().toLocalDateTime()
        );
        assertEquals(
                LocalDateTime.of(2026, 9, 10, 15, 45),
                loadedEvent.getTo().toLocalDateTime()
        );
    }

    @Test
    void load_backwardsDateOnlyEvent_skipsMalformedRecord() throws Exception {
        Path filePath = temporaryDirectory.resolve("data/tasks.txt");
        Storage storage = new Storage(filePath);
        String record = String.join(
                "\t",
                "E",
                "0",
                encode("backwards event"),
                encode("2026-09-11"),
                encode("2026-09-10")
        );
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, record, StandardCharsets.UTF_8);

        Storage.LoadResult result = storage.load();

        assertTrue(result.tasks().isEmpty());
        assertEquals(1, result.skippedLineCount());
    }

    @Test
    void load_invalidStoredDateTime_skipsMalformedRecord() throws Exception {
        Path filePath = temporaryDirectory.resolve("data/tasks.txt");
        Storage storage = new Storage(filePath);
        String record = String.join(
                "\t",
                "E",
                "0",
                encode("invalid event"),
                encode("2026-09-10T25:00:00"),
                encode("2026-09-10T26:00:00")
        );
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, record, StandardCharsets.UTF_8);

        Storage.LoadResult result = storage.load();

        assertTrue(result.tasks().isEmpty());
        assertEquals(1, result.skippedLineCount());
    }

    private static TaskList createTaskList() throws HaroldException {
        TaskList tasks = new TaskList();
        Todo todo = new Todo("read book");
        todo.markAsDone();
        tasks.add(todo);
        tasks.add(new Deadline("return book", LocalDate.of(2026, 9, 1)));
        tasks.add(new Event(
                "project meeting",
                LocalDate.of(2026, 9, 2),
                LocalDate.of(2026, 9, 3)
        ));
        return tasks;
    }

    private static void assertTodo(Task task) {
        Todo todo = assertInstanceOf(Todo.class, task);
        assertEquals("read book", todo.getDescription());
        assertTrue(todo.isDone());
    }

    private static void assertDeadline(Task task) {
        Deadline deadline = assertInstanceOf(Deadline.class, task);
        assertEquals("return book", deadline.getDescription());
        assertEquals(LocalDate.of(2026, 9, 1), deadline.getBy());
        assertFalse(deadline.isDone());
    }

    private static void assertEvent(Task task) {
        Event event = assertInstanceOf(Event.class, task);
        assertEquals("project meeting", event.getDescription());
        assertEquals(LocalDate.of(2026, 9, 2), event.getFrom().toLocalDate());
        assertEquals(LocalDate.of(2026, 9, 3), event.getTo().toLocalDate());
        assertFalse(event.hasTime());
        assertFalse(event.isDone());
    }

    private static String encode(String value) {
        return java.util.Base64.getEncoder().encodeToString(
                value.getBytes(StandardCharsets.UTF_8));
    }
}
