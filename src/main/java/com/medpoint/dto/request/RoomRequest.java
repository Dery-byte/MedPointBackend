package com.medpoint.dto.request;

import com.medpoint.enums.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RoomRequest {

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Status is required")
    private RoomStatus status;  // AVAILABLE | OCCUPIED | MAINTENANCE
}
