package harold;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HaroldTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void respond_addAndListTask_returnsExpectedMessages() {
        Harold harold = createHarold();

        CommandResult addResult = harold.respond("todo give Harold a treat");
        CommandResult listResult = harold.respond("list");

        assertEquals(
                "Got it. I've added this task:\n"
                        + "  [T][ ] give Harold a treat\n"
                        + "Now you have 1 task in the list.",
                addResult.message()
        );
        assertEquals(
                "Here are the tasks in your list:\n1.[T][ ] give Harold a treat",
                listResult.message()
        );
        assertFalse(addResult.isExit());
        assertFalse(listResult.isExit());
    }

    @Test
    void constructor_existingData_loadsPersistedTasks() {
        Harold firstSession = createHarold();
        firstSession.respond("deadline vet visit /by 2026-09-10");

        Harold secondSession = createHarold();
        CommandResult result = secondSession.respond("list");

        assertEquals(
                "Here are the tasks in your list:\n"
                        + "1.[D][ ] vet visit (by: Sep 10 2026)",
                result.message()
        );
    }

    @Test
    void respond_invalidCommand_returnsErrorWithoutExiting() {
        CommandResult result = createHarold().respond("fetch ball");

        assertEquals(
                "OOPS!!! I don't know what 'fetch ball' means. "
                        + "Try todo, deadline, event, list, find, mark, unmark, delete, or bye.",
                result.message()
        );
        assertFalse(result.isExit());
    }

    @Test
    void respond_deadlineWithMalformedByMarker_returnsSyntaxError() {
        CommandResult result = createHarold().respond(
                "deadline return book /byTomorrow 2026-09-11"
        );

        assertEquals(
                "OOPS!!! A deadline needs '/by <date or time>'. "
                        + "Try: deadline <description> /by <date or time>",
                result.message()
        );
        assertFalse(result.isExit());
    }

    @Test
    void respond_eventWithMalformedFromMarker_returnsSyntaxError() {
        CommandResult result = createHarold().respond(
                "event conference /fromTomorrow 2026-09-11 /to 2026-09-12"
        );

        assertEquals(
                "OOPS!!! An event needs '/from <start>'. "
                        + "Try: event <description> /from <start> /to <end>",
                result.message()
        );
        assertFalse(result.isExit());
    }

    @Test
    void respond_eventWithMalformedToMarker_returnsSyntaxError() {
        CommandResult result = createHarold().respond(
                "event conference /from 2026-09-11 /toTomorrow 2026-09-12"
        );

        assertEquals(
                "OOPS!!! An event needs '/to <end>'. "
                        + "Try: event <description> /from <start> /to <end>",
                result.message()
        );
        assertFalse(result.isExit());
    }

    @Test
    void respond_bye_returnsExitResult() {
        CommandResult result = createHarold().respond("bye");

        assertEquals("Goodbye! Please take me down soon hehe!", result.message());
        assertTrue(result.isExit());
    }

    private Harold createHarold() {
        return new Harold(temporaryDirectory.resolve("data/harold.txt"));
    }
}
