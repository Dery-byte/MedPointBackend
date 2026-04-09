package com.medpoint.config;

import com.medpoint.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService staffDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          @Qualifier("userDetailsServiceImpl") UserDetailsService staffDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.staffDetailsService = staffDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints
                .requestMatchers("/auth/**").permitAll()
                // Dev portal auth endpoints (public)
                .requestMatchers("/dev/auth/request", "/dev/auth/verify").permitAll()
                // Customer auth endpoints (public)
                .requestMatchers("/customers/register", "/customers/login").permitAll()
                // Store config: GET is public, PUT requires DEV role
                .requestMatchers(HttpMethod.GET, "/config").permitAll()
<<<<<<< HEAD
=======

                    // Paystack payment endpoints (public)
                    .requestMatchers("/payments/**").permitAll()
                    .requestMatchers("/webhooks/**").permitAll()
                    .requestMatchers("/getAllOnlinetransaction/**").permitAll()




>>>>>>> 3fb1b86c2d929ab2b748bf99fc540ac3025b120a
                .requestMatchers(HttpMethod.PUT, "/config").hasRole("DEV")
                // Dev profiles: requires DEV role
                .requestMatchers("/dev/profiles/**").hasRole("DEV")
                // Public storefront reads
                .requestMatchers(HttpMethod.GET, "/mart/products", "/mart/products/**", "/mart/categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/drugstore/drugs", "/drugstore/non-drug-items", "/drugstore/services").permitAll()
                .requestMatchers(HttpMethod.GET, "/hotel/rooms", "/hotel/room-categories", "/hotel/room-extras").permitAll()
                .requestMatchers(HttpMethod.GET, "/restaurant/menu-items", "/restaurant/tables").permitAll()
                // Storefront orders: POST is public (guest checkout), GET all is admin-only
                .requestMatchers(HttpMethod.POST, "/store/orders").permitAll()
                .requestMatchers(HttpMethod.GET, "/store/orders").hasAnyRole("SUPERADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/store/orders/*/status").hasAnyRole("SUPERADMIN", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/store/orders/my").authenticated()
                // Static uploaded files
                .requestMatchers("/uploads/**").permitAll()
                // Admin-only endpoints
                .requestMatchers("/admin/**").hasAnyRole("SUPERADMIN", "MANAGER")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(staffDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
