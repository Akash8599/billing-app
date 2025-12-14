package com.billingsystem.service;

import com.billingsystem.dto.CreateSalesOrderRequest;
import com.billingsystem.dto.SalesOrderResponse;
import com.billingsystem.exceptions.*;
import com.billingsystem.exceptions.ResourceNotFoundException;
import com.billingsystem.model.Product;
import com.billingsystem.model.SalesOrder;
import com.billingsystem.model.SalesOrderItem;
import com.billingsystem.repository.ProductRepository;
import com.billingsystem.repository.SalesOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class SalesOrderService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Create a new sales order
     * - Validate stock for all products
     * - Create order and items
     * - Reduce inventory
     * - Calculate totals with GST
     */
    public SalesOrderResponse createSalesOrder(CreateSalesOrderRequest request, String currentUser) throws InsufficientStockException {
        log.info("Creating sales order for customer: {}", request.getCustomerName());

        // Validate request
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Validate customer info
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer phone is required");
        }

        // Check stock for all products first
        Map<Long, Integer> productQuantities = new HashMap<>();
        for (CreateSalesOrderRequest.SalesOrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()
                    ));

            if (itemRequest.getQuantity() > product.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getQuantity() +
                                ", Requested: " + itemRequest.getQuantity()
                );
            }

            productQuantities.put(itemRequest.getProductId(), itemRequest.getQuantity());
        }

        // Create sales order
        SalesOrder salesOrder = SalesOrder.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .customerAddress(request.getCustomerAddress())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();

        // Generate order number (format: ORD-YYYY-MM-DD-XXXXX)
        String orderNumber = generateOrderNumber();
        salesOrder.setOrderNumber(orderNumber);

        // Create order items and reduce inventory
        List<SalesOrderItem> items = new ArrayList<>();
        for (CreateSalesOrderRequest.SalesOrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()
                    ));

            // Create order item
            SalesOrderItem item = SalesOrderItem.builder()
                    .salesOrder(salesOrder)
                    .product(product)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .productDescription(product.getDescription())
                    .quantity(itemRequest.getQuantity())
                    .sellingPrice(itemRequest.getSellingPrice() != null ?
                            itemRequest.getSellingPrice() :
                            BigDecimal.valueOf(product.getSellingPrice()))
                    .costPrice(BigDecimal.valueOf(product.getCostPrice()))
                    .gstRate(itemRequest.getGstRate() != null ?
                            itemRequest.getGstRate().intValue() :
                            product.getGstRate().intValue())
                    .discountPercent(itemRequest.getDiscountPercent())
                    .build();

            // Calculate item totals
            item.calculateTotals();
            items.add(item);

            log.info("Creating order item: Product ID={}, Name={}, Quantity={}",
                product.getId(), product.getName(), itemRequest.getQuantity());
        }

        // Set items and calculate totals
        salesOrder.setItems(items);
        salesOrder.calculateTotals();

        // SAVE ORDER FIRST
        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);
        log.info("✅ Sales order SAVED with order number: {}", savedOrder.getOrderNumber());

        // THEN reduce inventory AFTER order is saved
        log.info("📦 Starting inventory reduction for {} items", savedOrder.getItems().size());
        for (SalesOrderItem item : savedOrder.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found with id: " + item.getProduct().getId()
                ));

            int oldQuantity = product.getQuantity();
            int orderedQuantity = item.getQuantity();
            int newQuantity = oldQuantity - orderedQuantity;

            log.info("Reducing inventory for Product ID {}: {} - {} = {}",
                product.getId(), oldQuantity, orderedQuantity, newQuantity);

            // Update product quantity
            product.setQuantity(newQuantity);

            // Save product
            productRepository.save(product);
            log.info("✅ Saved product {} with new quantity: {}", product.getId(), newQuantity);
        }

        // Force flush to ensure all changes are committed
        productRepository.flush();
        log.info("✅ All inventory changes FLUSHED to database");

        return SalesOrderResponse.fromEntity(savedOrder);
    }

    /**
     * Get all sales orders
     */
    public List<SalesOrderResponse> getAllSalesOrders() {
        return salesOrderRepository.findAll().stream()
            .map(SalesOrderResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get sales order by ID
     */
    public SalesOrderResponse getSalesOrderById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));
        return SalesOrderResponse.fromEntity(order);
    }

    /**
     * Get sales order by order number
     */
    public SalesOrderResponse getSalesOrderByOrderNumber(String orderNumber) {
        SalesOrder order = salesOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with order number: " + orderNumber));
        return SalesOrderResponse.fromEntity(order);
    }

    /**
     * Get sales orders by customer phone
     */
    public List<SalesOrderResponse> getSalesOrdersByCustomerPhone(String phone) {
        return salesOrderRepository.findByCustomerPhone(phone).stream()
                .map(SalesOrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get unpaid orders
     */
    public List<SalesOrderResponse> getUnpaidOrders() {
        return salesOrderRepository.findUnpaidOrders().stream()
                .map(SalesOrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Mark order as paid
     */
    public SalesOrderResponse markOrderAsPaid(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));

        order.setPaymentStatus(SalesOrder.PaymentStatus.PAID);
        order.setPaidAt(LocalDateTime.now());

        SalesOrder savedOrder = salesOrderRepository.save(order);
        log.info("Order {} marked as paid", order.getOrderNumber());

        return SalesOrderResponse.fromEntity(savedOrder);
    }

    /**
     * Cancel order and restore inventory
     */
    public SalesOrderResponse cancelOrder(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));

        // Restore inventory
        for (SalesOrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + item.getProductId()
                    ));

            int newQuantity = product.getQuantity() + item.getQuantity();
            product.setQuantity(newQuantity);
            productRepository.save(product);

            log.info("Restored inventory for product {}: {} + {} = {}",
                    product.getId(),
                    product.getQuantity() - item.getQuantity(),
                    item.getQuantity(),
                    newQuantity
            );
        }

        // Update order status
        order.setStatus(SalesOrder.OrderStatus.CANCELLED);
        SalesOrder savedOrder = salesOrderRepository.save(order);
        log.info("Order {} cancelled", order.getOrderNumber());

        return SalesOrderResponse.fromEntity(savedOrder);
    }

    /**
     * Search orders
     */
    public List<SalesOrderResponse> searchOrders(String searchTerm) {
        return salesOrderRepository.searchOrders(searchTerm).stream()
                .map(SalesOrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get sales between dates
     */
    public List<SalesOrderResponse> getSalesOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return salesOrderRepository.findOrdersBetweenDates(startDate, endDate).stream()
                .map(SalesOrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        // Format: ORD-YYYY-MM-DD-XXXXX (where XXXXX is sequential)
        String prefix = "ORD-" + LocalDateTime.now().toLocalDate();

        // Count existing orders with this prefix
        List<SalesOrderResponse> ordersToday = getAllSalesOrders().stream()
                .filter(order -> order.getOrderNumber().startsWith(prefix))
                .collect(Collectors.toList());

        // Generate sequence number
        int sequence = ordersToday.size() + 1;
        return String.format("%s-%05d", prefix, sequence);
    }

    /**
     * Get sales summary
     */
    public Map<String, Object> getSalesSummary() {
        List<SalesOrder> allOrders = salesOrderRepository.findAll();

        BigDecimal totalRevenue = allOrders.stream()
                .map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = allOrders.stream()
                .map(SalesOrder::getTotalTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = allOrders.size();
        long paidOrders = salesOrderRepository.countByPaymentStatus(SalesOrder.PaymentStatus.PAID);
        long unpaidOrders = salesOrderRepository.countByPaymentStatus(SalesOrder.PaymentStatus.UNPAID);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalTax", totalTax);
        summary.put("totalOrders", totalOrders);
        summary.put("paidOrders", paidOrders);
        summary.put("unpaidOrders", unpaidOrders);
        summary.put("averageOrderValue", totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders)) :
                BigDecimal.ZERO);

        return summary;
    }
}