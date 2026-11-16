package ca.sfu.QuestLog;

import java.time.LocalDate;
import java.util.Set;

public class StreakCalculator {

    public static int calculateStreak(
            Set<LocalDate> completedDates,
            LocalDate today) {

        if (completedDates == null || completedDates.isEmpty()) {
            return 0;
        }

        LocalDate date = today;

        // If no task was completed today,
        // check whether the streak was active yesterday.
        if (!completedDates.contains(date)) {
            date = date.minusDays(1);
        }

        int streak = 0;

        while (completedDates.contains(date)) {
            streak++;
            date = date.minusDays(1);
        }

        return streak;
    }
}
