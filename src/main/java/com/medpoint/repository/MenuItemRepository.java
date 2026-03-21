package com.medpoint.repository;
import com.medpoint.entity.MenuItem;
import com.medpoint.enums.MenuItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByActiveTrueOrderByNameAsc();
    List<MenuItem> findByTypeAndActiveTrueOrderByNameAsc(MenuItemType type);
    List<MenuItem> findByCategoryAndActiveTrue(String category);
}
