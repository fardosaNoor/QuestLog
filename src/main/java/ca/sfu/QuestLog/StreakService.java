package ca.sfu.QuestLog;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class StreakService {

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
