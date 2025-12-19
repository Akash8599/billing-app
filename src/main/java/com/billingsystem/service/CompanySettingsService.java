package com.billingsystem.service;


import com.billingsystem.dto.CompanySettingsDTO;
import com.billingsystem.model.CompanySettings;
import com.billingsystem.repository.CompanySettingsRepository;
import com.billingsystem.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CompanySettingsService {

    @Autowired
    private CompanySettingsRepository companySettingsRepository;

    /**
     * Get company settings
     */
    public CompanySettingsDTO getCompanySettings() {
        log.info("Fetching company settings");
        CompanySettings settings = companySettingsRepository.findFirstByCreatedByOrderByIdAsc(SecurityUtils.currentUserId());

        if (settings == null) {
            log.warn("No company settings found");
            return null;
        }

        return mapToDTO(settings);
    }

    /**
     * Update company settings
     */
    public CompanySettingsDTO updateCompanySettings(CompanySettingsDTO dto) {
        log.info("Updating company settings");

        CompanySettings settings = companySettingsRepository.findFirstByCreatedByOrderByIdAsc(SecurityUtils.currentUserId());

        if (settings == null) {
            // Create new if doesn't exist
            settings = new CompanySettings();
        }

        // Update all fields
        settings.setCompanyName(dto.getCompanyName());
        settings.setGstin(dto.getGstin());
        settings.setUin(dto.getUin());
        settings.setAddress(dto.getAddress());
        settings.setCity(dto.getCity());
        settings.setState(dto.getState());
        settings.setPostalCode(dto.getPostalCode());
        settings.setContactPhone(dto.getContactPhone());
        settings.setContactEmail(dto.getContactEmail());

        settings.setBankName(dto.getBankName());
        settings.setAccountNumber(dto.getAccountNumber());
        settings.setIfscCode(dto.getIfscCode());
        settings.setAccountHolderName(dto.getAccountHolderName());

        settings.setStateCode(dto.getStateCode());
        settings.setWebsite(dto.getWebsite());
        settings.setLogo(dto.getLogo());
        settings.setSignature(dto.getSignature());
        settings.setAuthorizedSignatory(dto.getAuthorizedSignatory());
        settings.setAuthorizedSignatoryDesignation(dto.getAuthorizedSignatoryDesignation());
        settings.setUpdatedAt(LocalDateTime.now());
        settings.setUpdatedBy(SecurityUtils.currentUserId());
        settings.setCreatedBy(SecurityUtils.currentUserId());
        settings.setCreatedAt(LocalDateTime.now());

        CompanySettings saved = companySettingsRepository.save(settings);
        log.info("✅ Company settings updated successfully");

        return mapToDTO(saved);
    }

    /**
     * Map entity to DTO
     */
    private CompanySettingsDTO mapToDTO(CompanySettings settings) {
        return CompanySettingsDTO.builder()
                .companyName(settings.getCompanyName())
                .gstin(settings.getGstin())
                .uin(settings.getUin())
                .address(settings.getAddress())
                .city(settings.getCity())
                .state(settings.getState())
                .postalCode(settings.getPostalCode())
                .contactPhone(settings.getContactPhone())
                .contactEmail(settings.getContactEmail())
                .bankName(settings.getBankName())
                .accountNumber(settings.getAccountNumber())
                .ifscCode(settings.getIfscCode())
                .accountHolderName(settings.getAccountHolderName())
                .stateCode(settings.getStateCode())
                .website(settings.getWebsite())
                .logo(settings.getLogo())
                .signature(settings.getSignature())
                .authorizedSignatory(settings.getAuthorizedSignatory())
                .authorizedSignatoryDesignation(settings.getAuthorizedSignatoryDesignation())
                .build();
    }
}