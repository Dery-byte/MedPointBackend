package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CheckInRequest {
    @NotNull private Long roomId;       // Frontend sends roomId in body
    @NotBlank private String guestName;
    @NotBlank private String phone;
    private String nationality;
    private String address;
    @NotBlank private String idType;
    @NotBlank private String idNumber;
    @NotNull private LocalDate checkIn;
    @NotNull private LocalDate checkOut;
    private java.util.List<Long> extraIds; // optional extras at check-in
}
