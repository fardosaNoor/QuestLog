package ca.sfu.QuestLog;

import jakarta.persistence.*;

@Entity
public class OwnedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String studentEmail;

    protected OwnedItem() {}

    public OwnedItem(String itemName, String studentEmail) {
        this.itemName = itemName;
        this.studentEmail = studentEmail;
    }

    public Long getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }
}