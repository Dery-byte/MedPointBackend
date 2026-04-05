package com.medpoint.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService staffDetailsService;
    private final UserDetailsService customerDetailsService;
    private final UserDetailsService devDetailsService;

    @Autowired
    public JwtAuthFilter(
            JwtService jwtService,
            @Qualifier("userDetailsServiceImpl") UserDetailsService staffDetailsService,
            @Qualifier("customerDetailsService") UserDetailsService customerDetailsService,
            @Qualifier("devDetailsService") UserDetailsService devDetailsService) {
        this.jwtService = jwtService;
        this.staffDetailsService = staffDetailsService;
        this.customerDetailsService = customerDetailsService;
        this.devDetailsService = devDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            final String userEmail = jwtService.extractUsername(jwt);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String tokenType = jwtService.extractTokenType(jwt);
                UserDetails userDetails = switch (tokenType != null ? tokenType : "staff") {
                    case "customer" -> customerDetailsService.loadUserByUsername(userEmail);
                    case "dev"      -> devDetailsService.loadUserByUsername(userEmail);
                    default         -> staffDetailsService.loadUserByUsername(userEmail);
                };

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ignored) {
            // invalid token — proceed unauthenticated; Spring Security will reject protected endpoints
        }

        filterChain.doFilter(request, response);
    }
}
