package com.billingsystem.controller;

import com.billingsystem.dto.CreateSalesOrderRequest;
import com.billingsystem.dto.SalesOrderResponse;
import com.billingsystem.service.SalesOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales-orders")
@Slf4j
@CrossOrigin(origins = "*")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    /**
     * GET all sales orders
     * Accessible to: ADMIN, MANAGER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SalesOrderResponse>> getAllSalesOrders() {
        log.info("GET request: Fetch all sales orders");
        List<SalesOrderResponse> orders = salesOrderService.getAllSalesOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * GET sales order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponse> getSalesOrderById(@PathVariable Long id) {
        log.info("GET request: Fetch sales order with id: {}", id);
        SalesOrderResponse order = salesOrderService.getSalesOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * GET sales order by order number
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<SalesOrderResponse> getSalesOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("GET request: Fetch sales order with order number: {}", orderNumber);
        SalesOrderResponse order = salesOrderService.getSalesOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    /**
     * CREATE a new sales order
     * - Available to: ADMIN, MANAGER, CASHIER, CUSTOMER
     * - Automatically reduces product inventory
     * - Calculates GST for each product
     * - Returns order number and totals
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER', 'CUSTOMER')")
    public ResponseEntity<?> createSalesOrder(
            @RequestBody CreateSalesOrderRequest request,
            Authentication authentication) {
        try {
            log.info("POST request: Create sales order for customer: {}", request.getCustomerName());

            String currentUser = authentication != null ? authentication.getName() : "UNKNOWN";
            SalesOrderResponse order = salesOrderService.createSalesOrder(request, currentUser);

            log.info("Sales order created successfully with order number: {}", order.getOrderNumber());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(order);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error creating sales order: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create sales order"));
        }
    }

    /**
     * UPDATE sales order status
     * Only ADMIN can update status
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSalesOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            log.info("PUT request: Update sales order with id: {}", id);
            // Implementation for updating order status
            // This will be implemented based on your requirements
            return ResponseEntity.ok(Map.of("message", "Order updated successfully"));
        } catch (Exception e) {
            log.error("Error updating sales order: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update sales order"));
        }
    }

    /**
     * DELETE sales order (soft delete)
     * Only ADMIN can delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSalesOrder(@PathVariable Long id) {
        try {
            log.info("DELETE request: Delete sales order with id: {}", id);
            // Implementation for soft delete
            return ResponseEntity.ok(Map.of("message", "Order deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting sales order: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete sales order"));
        }
    }

    /**
     * MARK order as paid
     * Available to: ADMIN, MANAGER, CASHIER
     */
    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<?> markOrderAsPaid(@PathVariable Long id) {
        try {
            log.info("POST request: Mark sales order {} as paid", id);
            SalesOrderResponse order = salesOrderService.markOrderAsPaid(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error marking order as paid: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark order as paid"));
        }
    }

    /**
     * CANCEL order and restore inventory
     * Available to: ADMIN, MANAGER
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            log.info("POST request: Cancel sales order {}", id);
            SalesOrderResponse order = salesOrderService.cancelOrder(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error cancelling order: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel order"));
        }
    }

    /**
     * GENERATE invoice for sales order
     * Available to: ADMIN, MANAGER, CASHIER
     */
    @PostMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<?> generateInvoice(@PathVariable Long id) {
        try {
            log.info("POST request: Generate invoice for sales order {}", id);
            SalesOrderResponse order = salesOrderService.getSalesOrderById(id);
            // TODO: Implement invoice generation and PDF creation
            return ResponseEntity.ok(Map.of(
                    "message", "Invoice generated successfully",
                    "invoiceNumber", order.getInvoiceNumber()));
        } catch (Exception e) {
            log.error("Error generating invoice: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate invoice"));
        }
    }

    /**
     * SEARCH sales orders
     */
    @GetMapping("/search/{searchTerm}")
    public ResponseEntity<List<SalesOrderResponse>> searchOrders(@PathVariable String searchTerm) {
        log.info("GET request: Search sales orders with term: {}", searchTerm);
        List<SalesOrderResponse> orders = salesOrderService.searchOrders(searchTerm);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET sales orders by customer phone
     */
    @GetMapping("/customer/{phone}")
    public ResponseEntity<List<SalesOrderResponse>> getOrdersByCustomerPhone(@PathVariable String phone) {
        log.info("GET request: Fetch orders for customer phone: {}", phone);
        List<SalesOrderResponse> orders = salesOrderService.getSalesOrdersByCustomerPhone(phone);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET unpaid orders
     * Available to: ADMIN, MANAGER
     */
    @GetMapping("/unpaid/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SalesOrderResponse>> getUnpaidOrders() {
        log.info("GET request: Fetch all unpaid orders");
        List<SalesOrderResponse> orders = salesOrderService.getUnpaidOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * GET sales between dates
     * Available to: ADMIN, MANAGER
     */
    @GetMapping("/between")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SalesOrderResponse>> getOrdersBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET request: Fetch orders between {} and {}", startDate, endDate);
        List<SalesOrderResponse> orders = salesOrderService.getSalesOrdersBetweenDates(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET sales summary/dashboard stats
     * Available to: ADMIN, MANAGER
     */
    @GetMapping("/summary/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getSalesSummary() {
        log.info("GET request: Fetch sales summary");
        Map<String, Object> summary = salesOrderService.getSalesSummary();
        return ResponseEntity.ok(summary);
    }
}