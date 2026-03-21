package com.medpoint.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class MedicalServiceResponse {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private boolean active;
}
