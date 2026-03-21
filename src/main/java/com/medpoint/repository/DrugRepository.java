package com.medpoint.repository;
import com.medpoint.entity.Drug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface DrugRepository extends JpaRepository<Drug, Long> {
    List<Drug> findByActiveTrueOrderByNameAsc();
    @Query("SELECT d FROM Drug d WHERE d.active = true AND d.stock <= :threshold ORDER BY d.stock ASC")
    List<Drug> findLowStock(@Param("threshold") int threshold);
    @Query("SELECT d FROM Drug d WHERE d.active = true AND d.expiryDate IS NOT NULL AND d.expiryDate <= :cutoff ORDER BY d.expiryDate ASC")
    List<Drug> findExpiringSoon(@Param("cutoff") LocalDate cutoff);
}
