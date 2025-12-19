package com.billingsystem.controller;


import com.billingsystem.dto.CompanySettingsDTO;
import com.billingsystem.service.CompanySettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/company-settings")
@Slf4j
@CrossOrigin(origins = "*")
public class CompanySettingsController {

    @Autowired
    private CompanySettingsService companySettingsService;

    /**
     * GET company settings
     * Accessible to: All authenticated users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<?> getCompanySettings() {
        try {
            log.info("GET request: Fetch company settings");
            CompanySettingsDTO settings = companySettingsService.getCompanySettings();

            if (settings == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Company settings not configured"));
            }

            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            log.error("Error fetching company settings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch company settings"));
        }
    }

    /**
     * UPDATE company settings
     * Accessible to: ADMIN only
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<?> updateCompanySettings(@RequestBody CompanySettingsDTO dto) {
        try {
            log.info("PUT request: Update company settings");

            // Validate required fields
            if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Company name is required"));
            }

            if (dto.getGstin() == null || dto.getGstin().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "GSTIN is required"));
            }

            CompanySettingsDTO updated = companySettingsService.updateCompanySettings(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Company settings updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            log.error("Error updating company settings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update company settings"));
        }
    }
}