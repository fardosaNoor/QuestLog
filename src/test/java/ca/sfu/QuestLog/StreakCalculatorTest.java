
package ca.sfu.QuestLog;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreakCalculatorTest {

    @Test
    void countsConsecutiveDays() {
        LocalDate today = LocalDate.of(2026, 10, 31);

        Set<LocalDate> dates = Set.of(
                today,
                today.minusDays(1),
                today.minusDays(2)
        );

        int result = StreakCalculator.calculateStreak(dates, today);

        assertEquals(3, result);
    }

    @Test
    void keepsStreakActiveFromYesterday() {
        LocalDate today = LocalDate.of(2026, 10, 31);

        Set<LocalDate> dates = Set.of(
                today.minusDays(1),
                today.minusDays(2)
        );

        assertEquals(2,
                StreakCalculator.calculateStreak(dates, today));
    }

    @Test
    void resetsAfterMissingYesterday() {
        LocalDate today = LocalDate.of(2026, 10, 31);

        Set<LocalDate> dates = Set.of(
                today.minusDays(2),
                today.minusDays(3)
        );

        assertEquals(0,
                StreakCalculator.calculateStreak(dates, today));
    }

    @Test
    void returnsZeroForNoCompletedTasks() {
        LocalDate today = LocalDate.of(2026, 10, 31);

        assertEquals(0,
                StreakCalculator.calculateStreak(Set.of(), today));
    }
}
