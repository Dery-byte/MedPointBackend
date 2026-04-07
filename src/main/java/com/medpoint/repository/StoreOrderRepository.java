package com.medpoint.repository;

import com.medpoint.entity.StoreOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {
    List<StoreOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT o FROM StoreOrder o WHERE (:from IS NULL OR o.createdAt >= :from) AND (:to IS NULL OR o.createdAt <= :to) ORDER BY o.createdAt DESC")
    List<StoreOrder> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
