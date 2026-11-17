package ca.sfu.QuestLog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnedItemRepository extends JpaRepository<OwnedItem, Long> {

    List<OwnedItem> findByStudentEmail(String studentEmail);

    boolean existsByItemNameAndStudentEmail(String itemName, String studentEmail);
}