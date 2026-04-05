package com.medpoint.dto.request;

import lombok.Data;

@Data
public class UpdateRoomRequest {
    private String roomNumber;
    private Long categoryId;
    private String status;
}
