package com.medpoint.repository;
import com.medpoint.entity.NonDrugItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NonDrugItemRepository extends JpaRepository<NonDrugItem, Long> {
    List<NonDrugItem> findByActiveTrueOrderByNameAsc();
}
