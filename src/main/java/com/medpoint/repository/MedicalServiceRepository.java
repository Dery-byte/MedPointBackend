package com.medpoint.repository;
import com.medpoint.entity.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long> {
    List<MedicalService> findByActiveTrueOrderByNameAsc();
    List<MedicalService> findByCategoryAndActiveTrue(String category);
}
