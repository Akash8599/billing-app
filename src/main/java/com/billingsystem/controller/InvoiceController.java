package com.billingsystem.controller;


import com.billingsystem.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@Slf4j
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    /**
     * GET invoice data for a sales order
     * Accessible to: ADMIN, MANAGER, CASHIER
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<?> getInvoiceData(@PathVariable Long orderId) {
        try {
            log.info("GET request: Fetch invoice data for order: {}", orderId);
            Map<String, Object> invoiceData = invoiceService.generateInvoiceData(orderId);
            return ResponseEntity.ok(invoiceData);
        } catch (Exception e) {
            log.error("Error generating invoice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate invoice: " + e.getMessage()));
        }
    }
}