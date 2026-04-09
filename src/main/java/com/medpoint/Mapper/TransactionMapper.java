// mapper/TransactionMapper.java
package com.medpoint.Mapper;
import com.medpoint.dto.response.TransactionResponse;
import com.medpoint.dto.response.TransactionResponse.LineItemDto;
import com.medpoint.entity.Transaction;
import com.medpoint.entity.TransactionLineItem;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .reference(tx.getReference())
                .module(tx.getModule())
                .amount(tx.getAmount())
                .staffName(tx.getStaff().getFirstName() + " " + tx.getStaff().getLastName())
                .description(tx.getDescription())
                .status(tx.getStatus())
                .lineItems(mapLineItems(tx.getLineItems()))
                .cancelledByName(tx.getCancelledBy() != null
                        ? tx.getCancelledBy().getFirstName() + " " + tx.getCancelledBy().getLastName()
                        : null)
                .cancelledAt(tx.getCancelledAt())
                .createdAt(Instant.from(tx.getCreatedAt()))
                .build();
    }

    private List<LineItemDto> mapLineItems(List<TransactionLineItem> items) {
        if (items == null || items.isEmpty()) return List.of();
        return items.stream()
                .map(this::toLineItemDto)
                .toList();
    }

    private LineItemDto toLineItemDto(TransactionLineItem item) {
        return LineItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .kind(item.getKind())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}