package com.medpoint.repository;
import com.medpoint.entity.RestaurantTable;
import com.medpoint.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByTableNumber(int tableNumber);
    boolean existsByTableNumber(int tableNumber);
    List<RestaurantTable> findByStatus(TableStatus status);
    List<RestaurantTable> findAllByOrderByTableNumberAsc();
}
