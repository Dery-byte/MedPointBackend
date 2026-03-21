package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddRoomRequest {
    @NotBlank private String roomNumber;
    @NotNull private Long categoryId;
}
