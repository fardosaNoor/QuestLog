
package ca.sfu.QuestLog;

import jakarta.persistence.*;

@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String semester;

    @Column(nullable = false)
    private String studentEmail;

    protected Course() {}

    public Course(String code, String name, String semester,
                  String studentEmail) {
        this.code = code;
        this.name = name;
        this.semester = semester;
        this.studentEmail = studentEmail;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSemester() {
        return semester;
    }
}
