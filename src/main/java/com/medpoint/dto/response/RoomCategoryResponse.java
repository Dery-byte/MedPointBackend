package com.medpoint.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
@AllArgsConstructor  // makes sure the constructor is public
public class RoomCategoryResponse {
    private Long id;
    private String name;
    private BigDecimal pricePerNight;
    private int totalRooms;
    private int availableRooms;
    private int occupiedRooms;
}
