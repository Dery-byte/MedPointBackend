package com.medpoint.dto.response;
import com.medpoint.enums.TableStatus;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class RestaurantTableResponse {
    private Long id;
    private int tableNumber;
    private TableStatus status;
    private Long activeOrderId;
}
