package com.medpoint.controller;

import com.medpoint.dto.request.DevAuthRequest;
import com.medpoint.dto.request.DevVerifyRequest;
import com.medpoint.dto.response.DevAuthResponse;
import com.medpoint.dto.response.DevOtpResponse;
import com.medpoint.service.DevAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev/auth")
@RequiredArgsConstructor
public class DevAuthController {

    private final DevAuthService devAuthService;

    /** POST /dev/auth/request — send OTP to the developer's email */
    @PostMapping("/request")
    public ResponseEntity<DevOtpResponse> requestOtp(@Valid @RequestBody DevAuthRequest req) {
        return ResponseEntity.ok(devAuthService.requestOtp(req.getEmail()));
    }

    /** POST /dev/auth/verify — verify OTP, receive JWT */
    @PostMapping("/verify")
    public ResponseEntity<DevAuthResponse> verifyOtp(@Valid @RequestBody DevVerifyRequest req) {
        return ResponseEntity.ok(devAuthService.verifyOtp(req));
    }
}
