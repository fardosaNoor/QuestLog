package ca.sfu.QuestLog;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class FrozenDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentEmail;

    @Column(nullable = false)
    private LocalDate frozenDate;

    protected FrozenDay() {}

    public FrozenDay(String studentEmail, LocalDate frozenDate) {
        this.studentEmail = studentEmail;
        this.frozenDate = frozenDate;
    }

    public Long getId() {
        return id;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public LocalDate getFrozenDate() {
        return frozenDate;
    }
}