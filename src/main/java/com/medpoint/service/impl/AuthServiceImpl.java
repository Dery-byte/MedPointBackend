package com.medpoint.service.impl;
import com.medpoint.dto.request.LoginRequest;
import com.medpoint.dto.response.AuthResponse;
import com.medpoint.entity.User;
import com.medpoint.repository.UserRepository;
import com.medpoint.security.JwtService;
import com.medpoint.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));
        String token = jwtService.generateToken(user);

        // Flat response matching frontend types.ts AuthResponse
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .accessModules(user.getAccessModules())
                .manageModules(user.getManageModules())
                .active(user.isActive())
                .build();
    }
}
