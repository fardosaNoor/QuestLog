package ca.sfu.QuestLog;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BadgeCalculatorTest { 
        @Test
        void firstQuestIsLockedWhenNoTasksCompleted() {
        boolean result = BadgeCalculator.hasFirstQuest(0);

        assertFalse(result);
        }

        @Test
        void firstQuestUnlocksAfterOneTask() {
        boolean result = BadgeCalculator.hasFirstQuest(1);

        assertTrue(result);
        }

        @Test
        void firstQuestRemainsUnlockedAfterMultipleTasks() {
        boolean result = BadgeCalculator.hasFirstQuest(5);

        assertTrue(result);
        }
}
