package com.billingsystem.controller;

import com.billingsystem.model.Product;
import com.billingsystem.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("Request to create product: {}", product.getName());
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Fetching all active products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("Fetching product with ID: {}", id);
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.error("Product with ID {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productData) {
        log.info("Updating product with ID: {}", id);
        Product updated = productService.updateProduct(id, productData);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        log.info("Fetching low stock products");
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product with ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
