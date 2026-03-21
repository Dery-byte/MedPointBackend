package com.medpoint.repository;
import com.medpoint.entity.Booking;
import com.medpoint.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByRoomAndPaidFalse(Room room);
    List<Booking> findByPaidFalse();
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.paid = false AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    List<Booking> findOverlapping(@Param("roomId") Long roomId, @Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);
}
