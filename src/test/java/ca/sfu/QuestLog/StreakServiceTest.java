package ca.sfu.QuestLog;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class StreakServiceTest {
    private final StreakService service = new StreakService();

    private final ZoneId zone = ZoneId.of("America/Vancouver");

    private final LocalDate today = LocalDate.of(2026, 11, 14);

    @Test
    void noCompletedTasksReturnsZero() {
        int streak = service.getCurrentStreak(
                List.of(), today, zone);

        assertEquals(0, streak);
    }

    @Test
    void taskCompletedTodayStartsStreak() {
        int streak = service.getCurrentStreak(
                List.of(Instant.parse("2026-11-14T18:00:00Z")),
                today, zone);

        assertEquals(1, streak);
    }

    @Test
    void consecutiveDaysIncreaseStreak() {
        int streak = service.getCurrentStreak(
                List.of(
                    Instant.parse("2026-11-12T18:00:00Z"),
                    Instant.parse("2026-11-13T18:00:00Z"),
                    Instant.parse("2026-11-14T18:00:00Z")
                ),
                today, zone);

        assertEquals(3, streak);
    }

    @Test
    void multipleTasksSameDayCountOnce() {
        int streak = service.getCurrentStreak(
                List.of(
                    Instant.parse("2026-11-14T18:00:00Z"),
                    Instant.parse("2026-11-14T20:00:00Z")
                ),
                today, zone);

        assertEquals(1, streak);
    }

    @Test
    void missedDayBreaksStreak() {
        int streak = service.getCurrentStreak(
                List.of(
                    Instant.parse("2026-11-11T18:00:00Z"),
                    Instant.parse("2026-11-12T18:00:00Z")
                ),
                today, zone);

        assertEquals(0, streak);
    }

    @Test
    void yesterdayStillCountsAsActiveStreak() {
        int streak = service.getCurrentStreak(
                List.of(
                    Instant.parse("2026-11-12T18:00:00Z"),
                    Instant.parse("2026-11-13T18:00:00Z")
                ),
                today, zone);

        assertEquals(2, streak);
    }
}
