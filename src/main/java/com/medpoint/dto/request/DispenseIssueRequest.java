package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record DispenseIssueRequest(

        @NotEmpty
        List<ItemLineDto> items

) {
    public record ItemLineDto(
            @NotNull  Long        id,
            @NotBlank String      name,
            @NotBlank String      cat,
            @NotNull  @Positive   BigDecimal price,
            @NotNull  @Min(1)     Integer    qty
    ) {}
}