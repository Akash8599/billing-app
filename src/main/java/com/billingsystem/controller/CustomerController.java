package com.billingsystem.controller;

import com.billingsystem.dto.CustomerRequest;
import com.billingsystem.model.Customer;
import com.billingsystem.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<CustomerRequest> createCustomer(@RequestBody CustomerRequest customer) {
        CustomerRequest created = customerService.createOrUpdateCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<CustomerRequest>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<CustomerRequest> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<CustomerRequest> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerRequest customerData) {
        CustomerRequest updated = customerService.updateCustomer(id, customerData);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/quick-add")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Customer> quickAddCustomer(
            @RequestParam String phoneNumber,
            @RequestParam String name,
            @RequestParam(defaultValue = "RETAIL") String type) {
        Customer customer = customerService.getOrCreateCustomer(phoneNumber, name, type);
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
