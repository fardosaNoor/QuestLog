
package ca.sfu.QuestLog;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Epic 3: study plan generator.
// For each unfinished task, suggests study sessions spread over the days before
// the due date instead of cramming at the end.
//
// Rules used in this prototype:
//  - at most 2 hours of work per session, one session per day
//  - sessions are spaced roughly every 2 days and finish the day before the due date
//  - if there are not enough days left, sessions get longer and the task is flagged "crammed"
public final class StudyPlanner {

    static final double MAX_HOURS_PER_SESSION = 2.0;
    static final double DEFAULT_ESTIMATE_HOURS = 2.0;

    private StudyPlanner() {}

    public record Session(LocalDate date, double hours) {}

    public record PlanItem(String title, String courseCode, Instant due,
                           double totalHours, LocalDate startBy,
                           boolean crammed, List<Session> sessions) {}

    public static List<PlanItem> build(List<Task> tasks, ZoneId zone, Instant now) {
        LocalDate today = now.atZone(zone).toLocalDate();

        List<Task> open = tasks.stream()
                .filter(t -> !t.isDone())
                .sorted(Comparator.comparing(Task::getDueAt))
                .toList();

        List<PlanItem> plan = new ArrayList<>();

        for (Task task : open) {
            double hours = (task.getEstimateHours() != null && task.getEstimateHours() > 0)
                    ? task.getEstimateHours()
                    : DEFAULT_ESTIMATE_HOURS;

            LocalDate dueDay = task.getDueAt().atZone(zone).toLocalDate();

            // Days available to work: today up to the day before the due date.
            List<LocalDate> available = new ArrayList<>();
            for (LocalDate d = today; d.isBefore(dueDay); d = d.plusDays(1)) {
                available.add(d);
            }
            if (available.isEmpty()) {
                available.add(today); // due today or overdue: start now
            }

            int wanted = (int) Math.ceil(hours / MAX_HOURS_PER_SESSION);
            int sessions = Math.min(wanted, available.size());
            boolean crammed = wanted > available.size();

            // Use the last (sessions * 2) days before the due date, then pick evenly spaced days.
            int windowSize = Math.min(available.size(), sessions * 2);
            List<LocalDate> window = available.subList(available.size() - windowSize, available.size());

            double hoursPerSession = Math.round(hours / sessions * 10.0) / 10.0;
            List<Session> picked = new ArrayList<>();
            for (int i = 0; i < sessions; i++) {
                picked.add(new Session(window.get((i * window.size()) / sessions), hoursPerSession));
            }

            String courseCode = task.getCourse() != null ? task.getCourse().getCode() : "";

            plan.add(new PlanItem(task.getTitle(), courseCode, task.getDueAt(),
                    hours, picked.get(0).date(), crammed, picked));
        }

        return plan;
    }
}
