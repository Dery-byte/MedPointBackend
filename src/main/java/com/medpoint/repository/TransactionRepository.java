package com.medpoint.repository;
import com.medpoint.entity.Transaction;
import com.medpoint.entity.User;
import com.medpoint.enums.TransactionStatus;
import com.medpoint.enums.TxModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReference(String reference);
    List<Transaction> findAllByOrderByCreatedAtDesc();
    List<Transaction> findByModuleOrderByCreatedAtDesc(TxModule module);
    @Query("SELECT t FROM Transaction t WHERE (:module IS NULL OR t.module = :module) AND (:staff IS NULL OR t.staff = :staff) AND (:status IS NULL OR t.status = :status) AND (:fromDate IS NULL OR t.createdAt >= :fromDate) AND (:toDate IS NULL OR t.createdAt <= :toDate) ORDER BY t.createdAt DESC")
    List<Transaction> findFiltered(@Param("module") TxModule module, @Param("staff") User staff, @Param("status") TransactionStatus status, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.module = :module AND t.status = com.medpoint.enums.TransactionStatus.ACTIVE")
    BigDecimal sumActiveByModule(@Param("module") TxModule module);
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = com.medpoint.enums.TransactionStatus.ACTIVE")
    BigDecimal sumAllActive();




    @Query("""
        SELECT t FROM Transaction t
        WHERE (:module IS NULL OR t.module = :module)
          AND (:status IS NULL OR t.status = :status)
          AND (:staffId IS NULL OR t.staff.id = :staffId)
          AND (:from   IS NULL OR t.createdAt >= :from)
          AND (:to     IS NULL OR t.createdAt <= :to)
        ORDER BY t.createdAt DESC
    """)
    List<Transaction> findAllFiltered(
            @Param("module")  TxModule module,
            @Param("status")  TransactionStatus status,
            @Param("staffId") Long staffId,
            @Param("from")    Instant from,
            @Param("to")      Instant to
    );
}
