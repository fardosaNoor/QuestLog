package ca.sfu.QuestLog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RecurrenceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Vancouver");

    // Monday Nov 16 2026, 11:59 PM local time.
    private static final Instant MONDAY = LocalDateTime.of(2026, 11, 16, 23, 59).atZone(ZONE).toInstant();

    private static LocalDateTime local(Instant i) {
        return LocalDateTime.ofInstant(i, ZONE);
    }

    @Test
    void dailyWithNoDaysRepeatsTheNextDay() {
        Instant next = Recurrence.DAILY.next(MONDAY, Set.of(), ZONE);
        assertEquals(LocalDateTime.of(2026, 11, 17, 23, 59), local(next));
    }

    @Test
    void dailyWithDaysSkipsToTheNextChosenDay() {
        Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY);
        Instant thursday = Recurrence.DAILY.next(MONDAY, days, ZONE);
        assertEquals(LocalDateTime.of(2026, 11, 19, 23, 59), local(thursday));

        Instant nextMonday = Recurrence.DAILY.next(thursday, days, ZONE);
        assertEquals(LocalDateTime.of(2026, 11, 23, 23, 59), local(nextMonday));
    }

    @Test
    void weeklyAndMonthlyKeepTheSameTime() {
        assertEquals(LocalDateTime.of(2026, 11, 23, 23, 59),
                local(Recurrence.WEEKLY.next(MONDAY, null, ZONE)));
        assertEquals(LocalDateTime.of(2026, 12, 16, 23, 59),
                local(Recurrence.MONTHLY.next(MONDAY, null, ZONE)));
    }

    @Test
    void daysRoundTripThroughStorage() {
        Set<DayOfWeek> days = EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY);
        assertEquals("MONDAY,FRIDAY", Recurrence.joinDays(days));
        assertEquals(days, Recurrence.parseDays("MONDAY,FRIDAY"));
        assertNull(Recurrence.joinDays(Set.of()));
        assertTrue(Recurrence.parseDays(null).isEmpty());
    }

    @Test
    void completingARepeatingTaskCreatesTheNextOneOnlyOnce() {
        Task t = new Task("Weekly quiz", MONDAY, null, 1.0, "a@b.com", Recurrence.WEEKLY, null);

        assertNull(t.createNextOccurrence(ZONE)); // not done yet

        t.toggleDone();
        Task next = t.createNextOccurrence(ZONE);
        assertNotNull(next);
        assertEquals("Weekly quiz", next.getTitle());
        assertEquals(Recurrence.WEEKLY, next.getRecurrence());
        assertFalse(next.isDone());
        assertEquals(LocalDateTime.of(2026, 11, 23, 23, 59), local(next.getDueAt()));

        // Undo then Done again must not create a duplicate.
        t.toggleDone();
        t.toggleDone();
        assertNull(t.createNextOccurrence(ZONE));
    }

    @Test
    void nonRepeatingTaskNeverCreatesANextOne() {
        Task t = new Task("One-off", MONDAY, null, 1.0, "a@b.com");
        t.toggleDone();
        assertFalse(t.isRepeating());
        assertNull(t.createNextOccurrence(ZONE));
    }

    @Test
    void daysAreDroppedForNonDailyRepeats() {
        Task t = new Task("Lab", MONDAY, null, null, "a@b.com",
                Recurrence.WEEKLY, EnumSet.of(DayOfWeek.MONDAY));
        assertTrue(t.getRepeatDays().isEmpty());
        assertEquals("Every week", t.getRepeatLabel());
    }

    @Test
    void dailyLabelListsTheChosenDays() {
        Task t = new Task("Reading", MONDAY, null, null, "a@b.com",
                Recurrence.DAILY, EnumSet.of(DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY));
        assertEquals("Daily (Mon, Wed)", t.getRepeatLabel());
    }
}
