package com.medpoint.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class BookingResponse {
    private Long id;
    private String roomNumber;
    private String guestName;
    private String phone;
    private String nationality;
    private String address;
    private String idType;
    private String idNumber;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int nights;
    private BigDecimal baseAmount;
    private List<ExtraDto> extras;
    private BigDecimal totalCharged;
    private boolean paid;
    private Instant createdAt;

    @Data @Builder
    public static class ExtraDto {
        private String name;
        private BigDecimal price;
    }
}
