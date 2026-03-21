package com.medpoint.dto.response;
import com.medpoint.enums.MenuItemType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class MenuItemResponse {
    private Long id;
    private String name;
    private String category;
    private MenuItemType type;
    private BigDecimal price;
    private boolean active;
}
