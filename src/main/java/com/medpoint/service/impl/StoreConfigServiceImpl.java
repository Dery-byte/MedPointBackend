package com.medpoint.service.impl;

import com.medpoint.entity.StoreConfig;
import com.medpoint.repository.StoreConfigRepository;
import com.medpoint.service.StoreConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StoreConfigServiceImpl implements StoreConfigService {

    private static final Long CONFIG_ID = 1L;

    /** Minimal default JSON — frontend merges with its own DEFAULT_CONFIG */
    private static final String DEFAULT_JSON = "{}";

    private final StoreConfigRepository storeConfigRepository;

    @Override
    public String getConfig() {
        return storeConfigRepository.findById(CONFIG_ID)
                .map(StoreConfig::getConfigJson)
                .orElse(DEFAULT_JSON);
    }

    @Override
    public void saveConfig(String configJson) {
        StoreConfig row = storeConfigRepository.findById(CONFIG_ID)
                .orElse(new StoreConfig(CONFIG_ID, null, null));
        row.setConfigJson(configJson);
        row.setUpdatedAt(LocalDateTime.now());
        storeConfigRepository.save(row);
    }
}
