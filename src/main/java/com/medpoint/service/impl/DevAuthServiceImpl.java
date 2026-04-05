package com.medpoint.service.impl;

import com.medpoint.dto.request.DevVerifyRequest;
import com.medpoint.dto.response.DevAuthResponse;
import com.medpoint.dto.response.DevOtpResponse;
import com.medpoint.entity.DeveloperOtp;
import com.medpoint.repository.DeveloperOtpRepository;
import com.medpoint.repository.DeveloperRepository;
import com.medpoint.security.DevDetails;
import com.medpoint.service.DevAuthService;
import com.medpoint.service.EmailService;
import com.medpoint.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DevAuthServiceImpl implements DevAuthService {

    private final DeveloperRepository developerRepository;
    private final DeveloperOtpRepository developerOtpRepository;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.dev-mode:false}")
    private boolean devMode;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    public DevOtpResponse requestOtp(String email) {
        developerRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No developer account found for this email"));

        // Invalidate any existing unused tokens for this email
        developerOtpRepository.invalidateAllForEmail(email);

        // Generate 6-char alphanumeric OTP
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        String token = sb.toString();

        DeveloperOtp otp = DeveloperOtp.builder()
                .email(email)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        developerOtpRepository.save(otp);

        // Always send the email — even in dev mode so the real flow is exercised.
        // In dev mode, the token is also returned in the response body for convenience.
        emailService.sendDevOtp(email, token);

        if (devMode) {
            log.info("[DEV MODE] OTP for {}: {}", email, token);
            return DevOtpResponse.builder()
                    .message("Token sent to " + email + " (dev mode: also shown below)")
                    .devModeToken(token)
                    .build();
        }

        return DevOtpResponse.builder()
                .message("Token sent to " + email)
                .build();
    }

    @Override
    public DevAuthResponse verifyOtp(DevVerifyRequest req) {
        DeveloperOtp otp = developerOtpRepository
                .findTopByEmailAndUsedFalseOrderByExpiresAtDesc(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No active token found"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has expired");
        }
        if (!otp.getToken().equalsIgnoreCase(req.getToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        // Mark used
        otp.setUsed(true);
        developerOtpRepository.save(otp);

        // Load developer and generate JWT with type="dev"
        var developer = developerRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer not found"));

        DevDetails devDetails = new DevDetails(developer);
        String jwt = jwtService.generateToken(devDetails, "dev");

        return DevAuthResponse.builder()
                .token(jwt)
                .name(developer.getName())
                .email(developer.getEmail())
                .build();
    }
}
