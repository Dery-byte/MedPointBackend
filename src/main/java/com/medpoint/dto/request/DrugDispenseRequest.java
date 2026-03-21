package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

/** Payload sent when issuing drugs from the dispensary. */
@Data
public class DrugDispenseRequest {
    @NotEmpty private List<DrugLineItem> items;

    @Data
    public static class DrugLineItem {
        @NotNull private Long drugId;
        @Min(1) private int quantity;
    }
}
