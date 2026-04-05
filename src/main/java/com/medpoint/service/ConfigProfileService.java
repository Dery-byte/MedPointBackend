package com.medpoint.service;

import com.medpoint.dto.request.SaveConfigProfileRequest;
import com.medpoint.dto.response.ConfigProfileResponse;

import java.util.List;

public interface ConfigProfileService {
    List<ConfigProfileResponse> getAllProfiles();
    ConfigProfileResponse saveProfile(SaveConfigProfileRequest req);
    void deleteProfile(Long id);
}
