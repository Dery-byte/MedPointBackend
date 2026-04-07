package com.medpoint.repository;

import com.medpoint.entity.StoreOrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreOrderStatusHistoryRepository extends JpaRepository<StoreOrderStatusHistory, Long> {

    @Query("SELECT h FROM StoreOrderStatusHistory h WHERE h.order.id = :orderId ORDER BY h.changedAt ASC")
    List<StoreOrderStatusHistory> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT h FROM StoreOrderStatusHistory h WHERE h.order.id IN :orderIds ORDER BY h.changedAt ASC")
    List<StoreOrderStatusHistory> findByOrderIds(@Param("orderIds") List<Long> orderIds);
}
