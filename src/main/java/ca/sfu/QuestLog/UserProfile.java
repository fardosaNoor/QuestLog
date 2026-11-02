package ca.sfu.QuestLog;

import jakarta.persistence.*;

@Entity
public class UserProfile {

    @Id
    private Long userId;

    @Column(nullable = false)
    private int streakGoal = 7;

    protected UserProfile() {
    }

    public UserProfile(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public int getStreakGoal() {
        return streakGoal;
    }

    public void setStreakGoal(int streakGoal) {
        this.streakGoal = streakGoal;
    }
}