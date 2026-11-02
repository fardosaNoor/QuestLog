package ca.sfu.QuestLog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class StudyPlannerTest {

    private static final ZoneId ZONE = ZoneId.of("America/Vancouver");
    private static final Instant NOW = Instant.parse("2026-11-02T20:00:00Z");

    private Task task(String title, long hoursFromNow, Double estimate) {
        return new Task(title, NOW.plusSeconds(hoursFromNow * 3600), null, estimate, "a@b.com");
    }

    @Test
    void sessionsAreSpreadOverDifferentDaysAndEndBeforeDueDay() {
        Task t = task("Assignment 2", 24 * 10, 8.0);
        StudyPlanner.PlanItem p = StudyPlanner.build(List.of(t), ZONE, NOW).get(0);

        assertEquals(4, p.sessions().size());
        assertFalse(p.crammed());

        long distinctDays = p.sessions().stream()
                .map(StudyPlanner.Session::date).collect(Collectors.toSet()).size();
        assertEquals(4, distinctDays);

        LocalDate dueDay = t.getDueAt().atZone(ZONE).toLocalDate();
        LocalDate lastSession = p.sessions().get(3).date();
        assertTrue(lastSession.isBefore(dueDay));
    }

    @Test
    void taskWithTooLittleTimeIsFlaggedCrammed() {
        Task t = task("Big report", 30, 10.0);
        assertTrue(StudyPlanner.build(List.of(t), ZONE, NOW).get(0).crammed());
    }

    @Test
    void overdueTaskStartsToday() {
        Task t = task("Late lab", -24, 2.0);
        StudyPlanner.PlanItem p = StudyPlanner.build(List.of(t), ZONE, NOW).get(0);
        assertEquals(NOW.atZone(ZONE).toLocalDate(), p.startBy());
    }

    @Test
    void completedTasksAreLeftOutOfThePlan() {
        Task t = task("Done already", 48, 2.0);
        t.toggleDone();
        assertTrue(StudyPlanner.build(List.of(t), ZONE, NOW).isEmpty());
    }

    @Test
    void missingEstimateDefaultsToTwoHours() {
        Task t = task("No estimate", 24 * 5, null);
        assertEquals(2.0, StudyPlanner.build(List.of(t), ZONE, NOW).get(0).totalHours());
    }
}
