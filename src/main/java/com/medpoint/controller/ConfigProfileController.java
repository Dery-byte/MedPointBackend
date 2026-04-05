package com.medpoint.controller;

import com.medpoint.dto.request.SaveConfigProfileRequest;
import com.medpoint.dto.response.ConfigProfileResponse;
import com.medpoint.service.ConfigProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dev/profiles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DEV')")
public class ConfigProfileController {

    private final ConfigProfileService configProfileService;

    @GetMapping
    public ResponseEntity<List<ConfigProfileResponse>> getAll() {
        return ResponseEntity.ok(configProfileService.getAllProfiles());
    }

    @PostMapping
    public ResponseEntity<ConfigProfileResponse> create(@Valid @RequestBody SaveConfigProfileRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configProfileService.saveProfile(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        configProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
