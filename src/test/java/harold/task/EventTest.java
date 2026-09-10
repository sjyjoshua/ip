package harold.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class EventTest {
    @Test
    void clashesWith_overlappingTimedEvents_returnsTrue() {
        Event first = timedEvent("first", 12, 0, 13, 30);
        Event second = timedEvent("second", 13, 0, 14, 0);

        assertTrue(first.clashesWith(second));
        assertTrue(second.clashesWith(first));
    }

    @Test
    void clashesWith_eventsTouchAtBoundary_returnsFalse() {
        Event first = timedEvent("first", 12, 0, 13, 0);
        Event second = timedEvent("second", 13, 0, 14, 0);

        assertFalse(first.clashesWith(second));
        assertFalse(second.clashesWith(first));
    }

    @Test
    void clashesWith_dateOnlyEvent_returnsFalse() {
        Event dateOnly = new Event(
                "date only",
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 10)
        );
        Event timed = timedEvent("timed", 12, 0, 13, 0);

        assertFalse(dateOnly.clashesWith(timed));
        assertTrue(dateOnly.sharesDateWith(timed));
    }

    @Test
    void constructor_crossMidnightTimedEvent_isAccepted() {
        Event event = new Event(
                "overnight",
                LocalDateTime.of(2026, 9, 10, 23, 0),
                LocalDateTime.of(2026, 9, 11, 1, 0)
        );

        assertTrue(event.hasTime());
    }

    @Test
    void sharesDateWith_timedEventEndsAtMidnight_doesNotOccupyEndDate() {
        Event timed = new Event(
                "late event",
                LocalDateTime.of(2026, 9, 10, 23, 0),
                LocalDateTime.of(2026, 9, 11, 0, 0)
        );
        Event nextDay = new Event(
                "next day",
                LocalDate.of(2026, 9, 11),
                LocalDate.of(2026, 9, 11)
        );

        assertFalse(timed.sharesDateWith(nextDay));
    }

    @Test
    void constructor_invalidRanges_throwException() {
        assertThrows(IllegalArgumentException.class, () -> new Event(
                "backwards date",
                LocalDate.of(2026, 9, 11),
                LocalDate.of(2026, 9, 10)
        ));
        assertThrows(IllegalArgumentException.class, () -> new Event(
                "zero duration",
                LocalDateTime.of(2026, 9, 10, 12, 0),
                LocalDateTime.of(2026, 9, 10, 12, 0)
        ));
    }

    private static Event timedEvent(
            String description,
            int fromHour,
            int fromMinute,
            int toHour,
            int toMinute
    ) {
        return new Event(
                description,
                LocalDateTime.of(2026, 9, 10, fromHour, fromMinute),
                LocalDateTime.of(2026, 9, 10, toHour, toMinute)
        );
    }
}
