
package ca.sfu.QuestLog;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class StreakService {

    private final TaskRepository taskRepository;

    public StreakService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // NEW: Get streak using a student's real tasks
    public int getCurrentStreak(String studentEmail) {

        List<Task> tasks =
                taskRepository.findByStudentEmail(studentEmail);

        List<Instant> completedAtTimes = tasks.stream()
                .filter(Task::isDone)
                .map(Task::getCompletedAt)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        ZoneId zone = ZoneId.of("America/Vancouver");

        return getCurrentStreak(
                completedAtTimes,
                LocalDate.now(zone),
                zone
        );
    }

    // EXISTING: Convert completion times into streak days
    public int getCurrentStreak(
            List<Instant> completedAtTimes,
            LocalDate today,
            ZoneId zone) {

        if (completedAtTimes == null || completedAtTimes.isEmpty()) {
            return 0;
        }

        Set<LocalDate> completedDates = completedAtTimes.stream()
                .filter(time -> time != null)
                .map(time -> time.atZone(zone).toLocalDate())
                .collect(Collectors.toSet());

        return StreakCalculator.calculateStreak(
                completedDates, today);
    }
}
