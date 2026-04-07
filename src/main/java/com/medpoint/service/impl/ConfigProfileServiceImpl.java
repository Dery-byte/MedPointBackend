package com.medpoint.service.impl;

import com.medpoint.dto.request.SaveConfigProfileRequest;
import com.medpoint.dto.response.ConfigProfileResponse;
import com.medpoint.entity.ConfigProfile;
import com.medpoint.repository.ConfigProfileRepository;
import com.medpoint.service.ConfigProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigProfileServiceImpl implements ConfigProfileService {

    private final ConfigProfileRepository configProfileRepository;

    @Override
    public List<ConfigProfileResponse> getAllProfiles() {
        return configProfileRepository.findAllByOrderBySavedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ConfigProfileResponse saveProfile(SaveConfigProfileRequest req) {
        ConfigProfile profile = ConfigProfile.builder()
                .name(req.getName())
                .modulesJson(req.getModulesJson())
                .themePreset(req.getThemePreset())
                .build();
        return toResponse(configProfileRepository.save(profile));
    }

    @Override
    public void deleteProfile(Long id) {
        if (!configProfileRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        }
        configProfileRepository.deleteById(id);
    }

    private ConfigProfileResponse toResponse(ConfigProfile p) {
        return ConfigProfileResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .modulesJson(p.getModulesJson())
                .themePreset(p.getThemePreset())
                .savedAt(p.getSavedAt())
                .build();
    }
}
