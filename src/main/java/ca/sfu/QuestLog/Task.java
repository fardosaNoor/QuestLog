
package ca.sfu.QuestLog;

import jakarta.persistence.*;
import java.time.Instant;

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

    protected Task() {}

    public Task(String title, Instant dueAt, Course course,
                Double estimateHours, String studentEmail) {
        this.title = title;
        this.dueAt = dueAt;
        this.course = course;
        this.estimateHours = estimateHours;
        this.studentEmail = studentEmail;
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

    public void toggleDone() {
        this.done = !this.done;
    }
}
