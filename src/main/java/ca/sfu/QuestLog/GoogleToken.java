
package ca.sfu.QuestLog;

import jakarta.persistence.*;
import java.time.Instant;

// Epic 3: stores a student's Google OAuth tokens so we can read their calendar.
// NOTE: tokens are stored as plain text in this prototype. Encrypt them before real use.
@Entity
public class GoogleToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String studentEmail;

    @Column(nullable = false, length = 2048)
    private String accessToken;

    @Column(length = 2048)
    private String refreshToken;

    @Column(nullable = false)
    private Instant expiresAt;

    protected GoogleToken() {}

    public GoogleToken(String studentEmail, String accessToken,
                       String refreshToken, Instant expiresAt) {
        this.studentEmail = studentEmail;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void update(String accessToken, String refreshToken, Instant expiresAt) {
        this.accessToken = accessToken;
        if (refreshToken != null) {
            this.refreshToken = refreshToken;
        }
        this.expiresAt = expiresAt;
    }
}
