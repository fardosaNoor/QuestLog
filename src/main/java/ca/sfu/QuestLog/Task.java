
package ca.sfu.QuestLog;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

// Epic 3: a task a student wants to get done, optionally linked to a course.
@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Instant dueAt;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private Double estimateHours;

    @Column(nullable = false)
    private boolean done;

    @Column(nullable = false)
    private String studentEmail;

    private Instant completedAt;

    // Repeat settings. Nullable so tasks saved before this feature still load (null = NONE).
    @Enumerated(EnumType.STRING)
    private Recurrence recurrence;

    // Weekdays for a DAILY repeat, e.g. "MONDAY,WEDNESDAY". Null means every day.
    private String repeatDays;

    // Set once the next occurrence has been created, so Undo + Done doesn't make duplicates.
    private Boolean nextCreated;

    protected Task() {}

    public Task(String title, Instant dueAt, Course course,
                Double estimateHours, String studentEmail) {
        this(title, dueAt, course, estimateHours, studentEmail, Recurrence.NONE, null);
    }

    public Task(String title, Instant dueAt, Course course, Double estimateHours,
                String studentEmail, Recurrence recurrence, Set<DayOfWeek> repeatDays) {
        this.title = title;
        this.dueAt = dueAt;
        this.course = course;
        this.estimateHours = estimateHours;
        this.studentEmail = studentEmail;
        this.recurrence = recurrence == null ? Recurrence.NONE : recurrence;
        this.repeatDays = this.recurrence == Recurrence.DAILY ? Recurrence.joinDays(repeatDays) : null;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public Course getCourse() {
        return course;
    }

    public Double getEstimateHours() {
        return estimateHours;
    }

    public boolean isDone() {
        return done;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Recurrence getRecurrence() {
        return recurrence == null ? Recurrence.NONE : recurrence;
    }

    public Set<DayOfWeek> getRepeatDays() {
        return Recurrence.parseDays(repeatDays);
    }

    public boolean isRepeating() {
        return getRecurrence() != Recurrence.NONE;
    }

    public String getRepeatLabel() {
        return getRecurrence().describe(getRepeatDays());
    }

    // The next copy of a repeating task, due at its next scheduled time.
    // Returns null if the task doesn't repeat, isn't done, or its next copy already exists.
    public Task createNextOccurrence(ZoneId zone) {
        if (!isRepeating() || !done || Boolean.TRUE.equals(nextCreated)) {
            return null;
        }
        nextCreated = true;
        Instant nextDue = getRecurrence().next(dueAt, getRepeatDays(), zone);
        return new Task(title, nextDue, course, estimateHours, studentEmail,
                getRecurrence(), getRepeatDays());
    }

    public void toggleDone() {
        this.done = !this.done;

        if (this.done) {
            this.completedAt = Instant.now();
        } else {
            this.completedAt = null;
        }
    }
}
