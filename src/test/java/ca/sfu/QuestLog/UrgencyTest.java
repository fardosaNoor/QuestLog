package ca.sfu.QuestLog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UrgencyTest {

    private static final Instant NOW = Instant.parse("2026-11-02T20:00:00Z");

    private Instant inHours(long hours) {
        return NOW.plusSeconds(hours * 3600);
    }

    @Test
    void pastDueDateIsOverdue() {
        assertEquals("overdue", Urgency.of(inHours(-1), NOW).level());
    }

    @Test
    void lessThanTwoDaysIsUrgent() {
        assertEquals("urgent", Urgency.of(inHours(10), NOW).level());
    }

    @Test
    void lessThanAWeekIsSoon() {
        assertEquals("soon", Urgency.of(inHours(72), NOW).level());
    }

    @Test
    void moreThanAWeekIsLater() {
        assertEquals("later", Urgency.of(inHours(24 * 20), NOW).level());
    }
}
