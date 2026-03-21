package com.medpoint.repository;
import com.medpoint.entity.Room;
import com.medpoint.entity.RoomCategory;
import com.medpoint.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);
    boolean existsByRoomNumber(String roomNumber);
    List<Room> findByStatus(RoomStatus status);
    List<Room> findByCategory(RoomCategory category);
    List<Room> findAllByOrderByRoomNumberAsc();
}
