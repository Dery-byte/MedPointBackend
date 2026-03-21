package com.medpoint.dto.request;
import lombok.Data;
import java.util.List;

@Data
public class CheckOutRequest {
    private Long bookingId;           // Frontend sends bookingId
    private List<Long> extraIds;      // Optional extras at checkout
}
