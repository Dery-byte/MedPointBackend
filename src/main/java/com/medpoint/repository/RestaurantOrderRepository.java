package com.medpoint.repository;
import com.medpoint.entity.RestaurantOrder;
import com.medpoint.entity.RestaurantTable;
import com.medpoint.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {
    Optional<RestaurantOrder> findByTableAndStatus(RestaurantTable table, OrderStatus status);
    List<RestaurantOrder> findByStatus(OrderStatus status);
}
