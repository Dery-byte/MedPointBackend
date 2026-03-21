package com.medpoint.dto.request;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class TableRequest {
    @Min(1) private int tableNumber;
}
