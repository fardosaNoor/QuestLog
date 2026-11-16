package ca.sfu.QuestLog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
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

    // ---- CalendarEvent occurrences ----

    // Monday Nov 16 2026, 10:30-11:20 AM local time.
    private static final Instant LECTURE_START = LocalDateTime.of(2026, 11, 16, 10, 30).atZone(ZONE).toInstant();
    private static final Instant LECTURE_END = LocalDateTime.of(2026, 11, 16, 11, 20).atZone(ZONE).toInstant();
    private static final Instant WINDOW_FROM = LocalDateTime.of(2026, 11, 16, 0, 0).atZone(ZONE).toInstant();
    private static final Instant WINDOW_TO = LocalDateTime.of(2026, 11, 30, 0, 0).atZone(ZONE).toInstant();

    private static CalendarEvent event(Recurrence r, Set<DayOfWeek> days) {
        return new CalendarEvent("Lecture", LECTURE_START, LECTURE_END, r, days, "a@b.com");
    }

    @Test
    void oneOffEventAppearsOnce() {
        List<CalendarEvent.Occurrence> occ = event(Recurrence.NONE, null)
                .occurrencesBetween(WINDOW_FROM, WINDOW_TO, ZONE);
        assertEquals(1, occ.size());
        assertEquals(LECTURE_END, occ.get(0).end());
    }

    @Test
    void weeklyEventAppearsEachWeekWithTheSameLength() {
        List<CalendarEvent.Occurrence> occ = event(Recurrence.WEEKLY, null)
                .occurrencesBetween(WINDOW_FROM, WINDOW_TO, ZONE);
        assertEquals(2, occ.size()); // Nov 16 and Nov 23 (Nov 30 is past the window)
        assertEquals(LocalDateTime.of(2026, 11, 23, 10, 30), local(occ.get(1).start()));
        assertEquals(LocalDateTime.of(2026, 11, 23, 11, 20), local(occ.get(1).end()));
    }

    @Test
    void dailyEventOnlyAppearsOnChosenDays() {
        List<CalendarEvent.Occurrence> occ = event(Recurrence.DAILY,
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                .occurrencesBetween(WINDOW_FROM, WINDOW_TO, ZONE);
        assertEquals(6, occ.size()); // M/W/F for two weeks
        assertTrue(occ.stream().allMatch(o -> Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                .contains(o.start().atZone(ZONE).getDayOfWeek())));
    }

    @Test
    void dailyEventStartingOnAnUnchosenDaySkipsThatDay() {
        // Starts Monday but only repeats on Tuesdays.
        List<CalendarEvent.Occurrence> occ = event(Recurrence.DAILY, EnumSet.of(DayOfWeek.TUESDAY))
                .occurrencesBetween(WINDOW_FROM, WINDOW_TO, ZONE);
        assertEquals(2, occ.size());
        assertEquals(LocalDateTime.of(2026, 11, 17, 10, 30), local(occ.get(0).start()));
    }

    @Test
    void repeatingEventThatStartedEarlierStillShowsInTheWindow() {
        Instant monthAgo = LECTURE_START.minus(java.time.Duration.ofDays(28));
        CalendarEvent e = new CalendarEvent("Lab", monthAgo, null, Recurrence.WEEKLY, null, "a@b.com");
        List<CalendarEvent.Occurrence> occ = e.occurrencesBetween(WINDOW_FROM, WINDOW_TO, ZONE);
        assertEquals(2, occ.size());
        assertNull(occ.get(0).end());
    }

    @Test
    void daysAreDroppedForNonDailyRepeats() {
        CalendarEvent e = event(Recurrence.WEEKLY, EnumSet.of(DayOfWeek.MONDAY));
        assertTrue(e.getRepeatDays().isEmpty());
        assertEquals("Every week", e.getRepeatLabel());
        assertFalse(event(Recurrence.NONE, null).isRepeating());
    }

    @Test
    void dailyLabelListsTheChosenDays() {
        CalendarEvent e = event(Recurrence.DAILY, EnumSet.of(DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY));
        assertEquals("Daily (Mon, Wed)", e.getRepeatLabel());
    }
}
