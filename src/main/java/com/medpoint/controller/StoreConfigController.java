package com.medpoint.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.medpoint.service.StoreConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class StoreConfigController {

    private final StoreConfigService storeConfigService;

    /** GET /config — public, returns the store config JSON */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getConfig() {
        return ResponseEntity.ok(storeConfigService.getConfig());
    }

    /** PUT /config — dev-only, saves raw JSON body as the store config */
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEV')")
    public ResponseEntity<Void> saveConfig(@RequestBody JsonNode body) {
        storeConfigService.saveConfig(body.toString());
        return ResponseEntity.noContent().build();
    }
}
