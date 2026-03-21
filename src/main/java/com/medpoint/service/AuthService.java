package com.medpoint.service;
import com.medpoint.dto.request.LoginRequest;
import com.medpoint.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
