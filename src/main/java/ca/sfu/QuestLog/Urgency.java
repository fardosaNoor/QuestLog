
package ca.sfu.QuestLog;

import java.time.Duration;
import java.time.Instant;

// Epic 3: how urgent a task is, based only on how close its due date is.
public record Urgency(String level, long hoursLeft) {

    public static Urgency of(Instant due, Instant now) {
        long hours = Duration.between(now, due).toHours();

        String level;
        if (due.isBefore(now)) {
            level = "overdue";
        } else if (hours < 48) {
            level = "urgent";
        } else if (hours < 24 * 7) {
            level = "soon";
        } else {
            level = "later";
        }
        return new Urgency(level, hours);
    }
}
