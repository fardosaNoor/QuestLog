
package ca.sfu.QuestLog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleTokenRepository extends JpaRepository<GoogleToken, Long> {

    Optional<GoogleToken> findByStudentEmail(String studentEmail);
}
