package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

/** Payload for issuing a clinical service + optional prescribed drug/non-drug items. */
@Data
public class ServiceDispenseRequest {
    @NotEmpty private List<Long> serviceIds;
    private List<ItemLineItem> items;

    @Data
    public static class ItemLineItem {
        @NotNull private Long itemId;
        /** true = Drug, false = NonDrugItem */
        private boolean isDrug;
        @Min(1) private int quantity;
    }
}
