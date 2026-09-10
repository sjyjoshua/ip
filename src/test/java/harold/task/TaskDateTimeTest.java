package harold.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

class TaskDateTimeTest {
    @Test
    void parseInput_dateOnly_preservesDatePrecision() {
        TaskDateTime result = TaskDateTime.parseInput("2026-09-10");

        assertEquals(LocalDate.of(2026, 9, 10), result.toLocalDate());
        assertFalse(result.hasTime());
        assertEquals("Sep 10 2026", result.format());
        assertEquals("2026-09-10", result.formatForStorage());
    }

    @Test
    void parseInput_dateAndTime_preservesTimePrecision() {
        TaskDateTime result = TaskDateTime.parseInput("2026-09-10 1430");

        assertEquals(LocalDateTime.of(2026, 9, 10, 14, 30), result.toLocalDateTime());
        assertTrue(result.hasTime());
        assertEquals("Sep 10 2026, 2:30 PM", result.format());
        assertEquals("2026-09-10T14:30:00", result.formatForStorage());
    }

    @Test
    void parseStored_existingDateAndNewDateTime_loadsBothFormats() {
        TaskDateTime dateOnly = TaskDateTime.parseStored("2026-09-10");
        TaskDateTime timed = TaskDateTime.parseStored("2026-09-10T14:30:00");

        assertFalse(dateOnly.hasTime());
        assertEquals(LocalDate.of(2026, 9, 10), dateOnly.toLocalDate());
        assertTrue(timed.hasTime());
        assertEquals(LocalDateTime.of(2026, 9, 10, 14, 30), timed.toLocalDateTime());
    }

    @Test
    void parseInput_invalidFormats_throwException() {
        assertThrows(DateTimeParseException.class, () ->
                TaskDateTime.parseInput("10-09-2026"));
        assertThrows(DateTimeParseException.class, () ->
                TaskDateTime.parseInput("2026-09-10 2:30pm"));
        assertThrows(DateTimeParseException.class, () ->
                TaskDateTime.parseInput("2026-02-29 1430"));
    }
}
