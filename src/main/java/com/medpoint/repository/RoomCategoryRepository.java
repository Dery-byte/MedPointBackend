package com.medpoint.repository;
import com.medpoint.entity.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoomCategoryRepository extends JpaRepository<RoomCategory, Long> {
    Optional<RoomCategory> findByName(String name);
    boolean existsByName(String name);
}
