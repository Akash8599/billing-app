package com.billingsystem.controller;

import com.billingsystem.dto.CreatePurchaseOrderRequest;
import com.billingsystem.model.PurchaseOrder;
import com.billingsystem.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PurchaseOrderController {
    private final PurchaseOrderService poService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody CreatePurchaseOrderRequest request) {
        try {
            log.info("Request to create purchase order from supplier: {}", request.getSupplierName());
            PurchaseOrder po = poService.createPurchaseOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(po);
        } catch (Exception e) {
            log.error("Error creating purchase order: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        log.info("Fetching all purchase orders");
        return ResponseEntity.ok(poService.getAllPurchaseOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PurchaseOrder> getPurchaseOrderById(@PathVariable Long id) {
        log.info("Fetching purchase order with ID: {}", id);
        return poService.getPurchaseOrderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.error("Purchase order ID {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<PurchaseOrder>> getPendingPurchaseOrders() {
        log.info("Fetching pending purchase orders");
        return ResponseEntity.ok(poService.getPendingPurchaseOrders());
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PurchaseOrder> receivePurchaseOrder(@PathVariable Long id) {
        try {
            log.info("Request to receive purchase order ID: {}", id);
            PurchaseOrder po = poService.receivePurchaseOrder(id);
            return ResponseEntity.ok(po);
        } catch (Exception e) {
            log.error("Error receiving purchase order ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> cancelPurchaseOrder(@PathVariable Long id) {
        try {
            log.info("Request to cancel purchase order ID: {}", id);
            poService.cancelPurchaseOrder(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error cancelling purchase order ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
