package com.medpoint.dto.response;
import com.medpoint.enums.RoomStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class RoomResponse {
    private Long id;
    private String roomNumber;
    private RoomCategoryResponse category;  // nested — frontend reads category.id & category.name
    private RoomStatus status;
    private BigDecimal pricePerNight;
    private BookingResponse activeBooking;
}
