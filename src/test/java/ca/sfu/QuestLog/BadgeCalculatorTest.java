package ca.sfu.QuestLog;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BadgeCalculatorTest {

    @Test
    void unlocksFirstQuest() {
        List<String> badges =
                BadgeCalculator.getUnlockedBadges(1, 1);

        assertEquals(List.of("First Quest"), badges);
    }

    @Test
    void unlocksSevenDayStreak() {
        List<String> badges =
                BadgeCalculator.getUnlockedBadges(7, 5);

        assertEquals(
                List.of("First Quest", "7-Day Streak"),
                badges
        );
    }

    @Test
    void unlocksAllBadges() {
        List<String> badges =
                BadgeCalculator.getUnlockedBadges(7, 12);

        assertEquals(
                List.of("First Quest", "7-Day Streak", "Task Master"),
                badges
        );
    }

    @Test
    void keepsBadgesLocked() {
        List<String> badges =
                BadgeCalculator.getUnlockedBadges(0, 0);

        assertEquals(List.of(), badges);
    }
}
