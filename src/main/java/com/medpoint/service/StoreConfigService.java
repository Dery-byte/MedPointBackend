package com.medpoint.service;

public interface StoreConfigService {
    String getConfig();
    void saveConfig(String configJson);
}
