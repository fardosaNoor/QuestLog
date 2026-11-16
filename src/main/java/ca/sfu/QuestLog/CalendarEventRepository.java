package ca.sfu.QuestLog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByStudentEmailOrderByStartAtAsc(String studentEmail);

    Optional<CalendarEvent> findByIdAndStudentEmail(Long id, String studentEmail);
}
