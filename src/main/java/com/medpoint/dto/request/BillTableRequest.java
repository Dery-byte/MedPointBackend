package com.medpoint.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillTableRequest {
    @NotNull private Long tableId;
}
