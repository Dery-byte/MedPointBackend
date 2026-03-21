package com.medpoint.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class RoomExtraResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private boolean active;
}
