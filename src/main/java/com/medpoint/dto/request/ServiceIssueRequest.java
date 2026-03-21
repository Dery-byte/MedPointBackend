// dto/request/ServiceIssueRequest.java
package com.medpoint.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ServiceIssueRequest(

        @NotEmpty List<ServiceLineDto> services,
        List<ItemLineDto>              items

) {
    public record ServiceLineDto(
            @NotNull  Long       id,
            @NotBlank String     name,
            @NotBlank String     cat,
            @NotNull  @Positive BigDecimal price
    ) {}

    public record ItemLineDto(
            @NotNull  Long       id,
            @NotBlank String     name,
            @NotBlank String     cat,
            @NotNull  @Positive BigDecimal price,
            @NotNull  @Min(1)   Integer    qty
    ) {}
}