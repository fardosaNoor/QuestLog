package ca.sfu.QuestLog;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FrozenDayRepository extends JpaRepository<FrozenDay, Long> {

    List<FrozenDay> findByStudentEmail(String studentEmail);

    boolean existsByStudentEmailAndFrozenDate(String studentEmail, LocalDate frozenDate);
}