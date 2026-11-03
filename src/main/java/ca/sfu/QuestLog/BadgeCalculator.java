package ca.sfu.QuestLog;

import java.util.ArrayList;
import java.util.List;

public class BadgeCalculator {

    public static List<String> getUnlockedBadges(
            int currentStreak,
            int completedTasks) {

        List<String> badges = new ArrayList<>();

        if (completedTasks >= 1) {
            badges.add("First Quest");
        }

        if (currentStreak >= 7) {
            badges.add("7-Day Streak");
        }

        if (completedTasks >= 10) {
            badges.add("Task Master");
        }

        return badges;
    }
}
