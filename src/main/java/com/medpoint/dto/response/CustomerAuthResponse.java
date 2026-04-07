package com.medpoint.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CustomerAuthResponse {
    private String token;
    private Long id;
    private String name;
    private String email;
    private String phone;
}
