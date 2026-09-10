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
    void respond_overlappingTimedEvent_addsTaskAndReportsClash() {
        Harold harold = createHarold();
        harold.respond("event lunch /from 2026-09-10 1200 /to 2026-09-10 1330");

        CommandResult result = harold.respond(
                "event meeting /from 2026-09-10 1300 /to 2026-09-10 1400"
        );

        assertEquals(
                "Got it. I've added this task:\n"
                        + "  [E][ ] meeting (from: Sep 10 2026, 1:00 PM "
                        + "to: Sep 10 2026, 2:00 PM)\n"
                        + "Now you have 2 tasks in the list.\n\n"
                        + "Heads up! This event clashes with:\n"
                        + "1.[E][ ] lunch (from: Sep 10 2026, 12:00 PM "
                        + "to: Sep 10 2026, 1:30 PM)",
                result.message()
        );
    }

    @Test
    void respond_dateOnlyEventsShareDate_addsTaskAndSuggestsTimes() {
        Harold harold = createHarold();
        harold.respond("event conference /from 2026-09-10 /to 2026-09-11");

        CommandResult result = harold.respond(
                "event workshop /from 2026-09-11 /to 2026-09-11"
        );

        assertEquals(
                "Got it. I've added this task:\n"
                        + "  [E][ ] workshop (from: Sep 11 2026 to: Sep 11 2026)\n"
                        + "Now you have 2 tasks in the list.\n\n"
                        + "Heads up! These events share a date, but at least one has no time:\n"
                        + "1.[E][ ] conference (from: Sep 10 2026 to: Sep 11 2026)\n"
                        + "Add times using yyyy-MM-dd HHmm if you want Harold to check "
                        + "for a precise clash.",
                result.message()
        );
    }

    @Test
    void respond_mixedEventPrecision_returnsValidationError() {
        CommandResult result = createHarold().respond(
                "event meeting /from 2026-09-10 1300 /to 2026-09-10"
        );

        assertEquals(
                "OOPS!!! Use times for both /from and /to, or omit times from both.",
                result.message()
        );
    }

    @Test
    void respond_invalidEventRanges_returnSpecificErrors() {
        Harold harold = createHarold();

        CommandResult backwardsDate = harold.respond(
                "event holiday /from 2026-09-11 /to 2026-09-10"
        );
        CommandResult zeroDuration = harold.respond(
                "event meeting /from 2026-09-10 1300 /to 2026-09-10 1300"
        );

        assertEquals(
                "OOPS!!! The event end date cannot be before its start date.",
                backwardsDate.message()
        );
        assertEquals(
                "OOPS!!! The event end date and time must be after its start date and time.",
                zeroDuration.message()
        );
    }

    @Test
    void respond_multipleTimedClashes_reportsOriginalTaskNumbersInOrder() {
        Harold harold = createHarold();
        harold.respond("event first /from 2026-09-10 1200 /to 2026-09-10 1400");
        harold.respond("todo unrelated");
        harold.respond("event second /from 2026-09-10 1300 /to 2026-09-10 1500");

        CommandResult result = harold.respond(
                "event candidate /from 2026-09-10 1330 /to 2026-09-10 1430"
        );

        assertTrue(result.message().contains(
                "Heads up! This event clashes with:\n"
                        + "1.[E][ ] first (from: Sep 10 2026, 12:00 PM "
                        + "to: Sep 10 2026, 2:00 PM)\n"
                        + "3.[E][ ] second (from: Sep 10 2026, 1:00 PM "
                        + "to: Sep 10 2026, 3:00 PM)"
        ));
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
