package com.billingsystem.service;

import com.billingsystem.model.Product;
import com.billingsystem.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final GSTService gstService;

    /**
     * Create new product
     */
    public Product createProduct(Product product) {
        if (!gstService.isValidGSTRate(product.getGstRate())) {
            throw new RuntimeException("Invalid GST rate. Use: 5, 12, 18, or 28");
        }

        product.setCreatedAt(System.currentTimeMillis());
        product.setUpdatedAt(System.currentTimeMillis());
        return productRepository.save(product);
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Get product by ID
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get product by SKU
     */
    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    /**
     * Update product details (not quantity)
     */
    public Product updateProduct(Long id, Product productData) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!gstService.isValidGSTRate(productData.getGstRate())) {
            throw new RuntimeException("Invalid GST rate");
        }

        product.setName(productData.getName());
        product.setDescription(productData.getDescription());
        product.setCostPrice(productData.getCostPrice());
        product.setSellingPrice(productData.getSellingPrice());
        product.setGstRate(productData.getGstRate());
        product.setLowStockAlert(productData.getLowStockAlert());
        product.setUpdatedAt(System.currentTimeMillis());
        product.setQuantity(productData.getQuantity());

        return productRepository.save(product);
    }

    /**
     * Get low stock products (quantity <= alert level)
     */
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    /**
     * Update product quantity (internal use)
     */
    public void updateQuantity(Long productId, int newQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setQuantity(newQuantity);
        product.setUpdatedAt(System.currentTimeMillis());
        productRepository.save(product);
    }

    /**
     * Delete product
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
