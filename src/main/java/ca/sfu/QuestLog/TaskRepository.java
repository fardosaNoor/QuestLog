
package ca.sfu.QuestLog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStudentEmail(String studentEmail);

    Optional<Task> findByIdAndStudentEmail(Long id, String studentEmail);
}
