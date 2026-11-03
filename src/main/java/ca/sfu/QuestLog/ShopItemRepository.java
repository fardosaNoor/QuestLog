package ca.sfu.QuestLog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {

    List<ShopItem> findAllByOrderByPriceAsc();
}