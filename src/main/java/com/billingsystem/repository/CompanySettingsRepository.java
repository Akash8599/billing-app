package com.billingsystem.repository;


import com.billingsystem.model.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {

    // Get the first (or only) company settings
    CompanySettings findFirstByOrderByIdAsc();
}
