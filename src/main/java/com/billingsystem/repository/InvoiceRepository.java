package com.billingsystem.repository;

import com.billingsystem.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate >= ?1 AND i.invoiceDate < ?2 ORDER BY i.invoiceDate DESC")
    List<Invoice> findByDateRange(Long startDate, Long endDate);
    @Query("SELECT i FROM Invoice i WHERE i.status = 'COMPLETED' ORDER BY i.invoiceDate DESC")
    List<Invoice> findCompletedInvoices();
}
