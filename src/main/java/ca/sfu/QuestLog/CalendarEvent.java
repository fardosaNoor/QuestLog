package ca.sfu.QuestLog;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Epic 3: something scheduled on the student's calendar (a lecture, a meeting, a shift).
// Unlike a Task it isn't "done"; it can repeat daily, weekly or monthly.
@Entity
@Table(name = "calendar_event")
public class CalendarEvent {

    // Safety cap so a very old daily event can't loop forever when building the calendar.
    private static final int MAX_STEPS = 5000;

    // One occurrence of the event, with its own start and end.
    public record Occurrence(CalendarEvent event, Instant start, Instant end) {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Instant startAt;

    // Optional; null means the event has no set end time.
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recurrence recurrence;

    // Weekdays for a DAILY repeat, e.g. "MONDAY,WEDNESDAY". Null means every day.
    private String repeatDays;

    @Column(nullable = false)
    private String studentEmail;

    protected CalendarEvent() {}

    public CalendarEvent(String title, Instant startAt, Instant endAt, Recurrence recurrence,
                         Set<DayOfWeek> repeatDays, String studentEmail) {
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.recurrence = recurrence == null ? Recurrence.NONE : recurrence;
        this.repeatDays = this.recurrence == Recurrence.DAILY ? Recurrence.joinDays(repeatDays) : null;
        this.studentEmail = studentEmail;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public Set<DayOfWeek> getRepeatDays() {
        return Recurrence.parseDays(repeatDays);
    }

    public boolean isRepeating() {
        return recurrence != Recurrence.NONE;
    }

    public String getRepeatLabel() {
        return recurrence.describe(getRepeatDays());
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    // Every occurrence that starts within [from, to].
    public List<Occurrence> occurrencesBetween(Instant from, Instant to, ZoneId zone) {
        Duration length = endAt == null ? null : Duration.between(startAt, endAt);
        Set<DayOfWeek> days = getRepeatDays();
        List<Occurrence> result = new ArrayList<>();

        // A DAILY event limited to certain weekdays only counts on those days,
        // including its very first date.
        Instant start = startAt;
        if (recurrence == Recurrence.DAILY && !days.isEmpty()
                && !days.contains(start.atZone(zone).getDayOfWeek())) {
            start = recurrence.next(start, days, zone);
        }

        for (int i = 0; start != null && !start.isAfter(to) && i < MAX_STEPS; i++) {
            if (!start.isBefore(from)) {
                result.add(new Occurrence(this, start, length == null ? null : start.plus(length)));
            }
            start = recurrence.next(start, days, zone);
        }
        return result;
    }
}
