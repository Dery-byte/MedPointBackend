package com.medpoint.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkDrugRequest {
    @NotEmpty
    private List<DrugRequest> drugs;
}
