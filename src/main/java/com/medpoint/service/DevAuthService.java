package com.medpoint.service;

import com.medpoint.dto.request.DevVerifyRequest;
import com.medpoint.dto.response.DevAuthResponse;
import com.medpoint.dto.response.DevOtpResponse;

public interface DevAuthService {
    DevOtpResponse requestOtp(String email);
    DevAuthResponse verifyOtp(DevVerifyRequest req);
}
