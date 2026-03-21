package com.medpoint.repository;
import com.medpoint.entity.RoomExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomExtraRepository extends JpaRepository<RoomExtra, Long> {
    List<RoomExtra> findByActiveTrue();
}
