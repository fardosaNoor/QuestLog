
package ca.sfu.QuestLog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByStudentEmailOrderByCodeAsc(String studentEmail);
}

