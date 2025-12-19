package com.billingsystem.service;

import com.billingsystem.dto.CreateSalesOrderRequest;
import com.billingsystem.dto.SalesOrderResponse;
import com.billingsystem.exceptions.*;
import com.billingsystem.exceptions.ResourceNotFoundException;
import com.billingsystem.model.Customer;
import com.billingsystem.model.Product;
import com.billingsystem.model.SalesOrder;
import com.billingsystem.model.SalesOrderItem;
import com.billingsystem.repository.CustomerRepository;
import com.billingsystem.repository.ProductRepository;
import com.billingsystem.repository.SalesOrderRepository;
import com.billingsystem.utils.CommonUtils;
import com.billingsystem.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Create a new sales order
     * - Validate stock for all products
     * - Create order and items
     * - Reduce inventory
     * - Calculate totals with GST
     */
    public SalesOrderResponse createSalesOrder(CreateSalesOrderRequest request, String currentUser) throws InsufficientStockException {
        log.info("Creating sales order for customer: {}", request.getCustomerName());

        Customer customer = saveOrUpdateCustomer(request);

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
                .customerState(request.getCustomerState())
                .customerStateCode(request.getCustomerStateCode())
                .customerGstIn(request.getCustomerGstIn())
                .notes(request.getNotes())
                .invoiceDate(LocalDateTime.now())
                .createdBy(SecurityUtils.currentUsername())
                .createdAt(LocalDateTime.now())
                .build();

        // Generate order number (format: ORD-YYYY-MM-DD-XXXXX)
        String orderNumber = generateOrderNumber();
        salesOrder.setOrderNumber(orderNumber);
        salesOrder.setInvoiceNumber(CommonUtils.generateInvoiceNumberFromOrder(orderNumber));

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
//                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .productDescription(product.getDescription())
                    .quantity(itemRequest.getQuantity())
                    .snapshotSellingPrice(itemRequest.getSellingPrice() != null ?
                            itemRequest.getSellingPrice() :
                            BigDecimal.valueOf(product.getSellingPrice()))
                    .snapshotCostPrice(BigDecimal.valueOf(product.getCostPrice()))
                    .snapshotGstRate(itemRequest.getGstRate() != null ?
                            itemRequest.getGstRate().intValue() :
                            product.getGstRate().intValue())
                    .discountPercent(itemRequest.getDiscountPercent())
                    .createdAt(LocalDateTime.now())
                    .createdBy(SecurityUtils.currentUsername())
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
            product.setUpdatedBy(SecurityUtils.currentUsername());
            product.setUpdatedAt(System.currentTimeMillis());

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
     * ✅ KEY METHOD: Save or update customer to database
     * - If customer with same phone exists → Update it
     * - If customer is new → Save it to database
     * - Always returns saved customer from database
     */
    private Customer saveOrUpdateCustomer(CreateSalesOrderRequest request) {
        String phoneNumber = request.getCustomerPhone();
        String customerName = request.getCustomerName();

        // Try to find existing customer by phone number
        Optional<Customer> existingCustomer = customerRepository.findByPhoneNumber(phoneNumber);

        Customer customer;
        if (existingCustomer.isPresent()) {
            // Update existing customer
            customer = existingCustomer.get();
            customer.setName(customerName);
            customer.setEmail(request.getCustomerEmail());
            customer.setAddress(request.getCustomerAddress());
            customer.setGstNumber(request.getCustomerGstIn() != null ? request.getCustomerGstIn() : "");
            customer.setUpdatedAt(System.currentTimeMillis());
            customer.setUpdatedBy(SecurityUtils.currentUsername());
        } else {
            // Create new customer
            customer = new Customer();
            customer.setName(customerName);
            customer.setPhoneNumber(phoneNumber);
            customer.setEmail(request.getCustomerEmail());
            customer.setAddress(request.getCustomerAddress());
            customer.setGstNumber(request.getCustomerGstIn() != null ? request.getCustomerGstIn() : "");
            customer.setCreatedAt(System.currentTimeMillis());
//            customer.setUpdatedAt(System.currentTimeMillis());
            customer.setCreatedBy(SecurityUtils.currentUsername());
        }

        // ✅ SAVE TO DATABASE
        return customerRepository.save(customer);
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
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(SecurityUtils.currentUsername());

        SalesOrder savedOrder = salesOrderRepository.save(order);
        log.info("Order {} marked as paid", order.getOrderNumber());

        return SalesOrderResponse.fromEntity(savedOrder);
    }

    /**
     * ════════════════════════════════════════════════════════════════════════════
     * CANCEL ORDER - Restore Inventory
     * ════════════════════════════════════════════════════════════════════════════
     *
     * Fixed to use product relationship instead of @Transient productId
     *
     * Key changes:
     * 1. Use item.getProduct().getId() instead of item.getProductId()
     * 2. Product is already loaded (eager fetch), no extra query needed
     * 3. Snapshot data stays intact in sales_order_items
     * 4. Just restore the product quantity
     */
    public SalesOrderResponse cancelOrder(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));

        log.info("Cancelling order: {}", order.getOrderNumber());

        // Restore inventory for all items in the order
        for (SalesOrderItem item : order.getItems()) {
            // ✅ FIXED: Use item.getProduct() instead of item.getProductId()
            // Since product is @ManyToOne with FetchType.EAGER, it's already loaded
            Product product = item.getProduct();

            // If product is deleted (soft deleted), still restore quantity
            // from another product reference or skip if null
            if (product == null) {
                log.warn("Product reference is null for order item {}", item.getId());
                log.warn("Snapshot data preserved: {} x {} units",
                        item.getProductName(),
                        item.getQuantity());
                // Continue - snapshot data is intact, just can't restore inventory
                continue;
            }

            // Calculate new quantity
            int oldQuantity = product.getQuantity();
            int restoreQuantity = item.getQuantity();
            int newQuantity = oldQuantity + restoreQuantity;

            // Restore inventory
            product.setQuantity(newQuantity);
            product.setUpdatedAt(System.currentTimeMillis());
            productRepository.save(product);

            log.info("Restored inventory - Product ID: {}, {} + {} = {}",
                    product.getId(),
                    oldQuantity,
                    restoreQuantity,
                    newQuantity);
        }

        // Update order status to CANCELLED
        order.setStatus(SalesOrder.OrderStatus.CANCELLED);
        order.setUpdatedBy(SecurityUtils.currentUsername());
        order.setUpdatedAt(LocalDateTime.now());
        SalesOrder savedOrder = salesOrderRepository.save(order);

        log.info("✅ Order {} cancelled successfully", order.getOrderNumber());

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
        summary.put("averageOrderValue",
                totalOrders > 0
                        ? totalRevenue.divide(
                        BigDecimal.valueOf(totalOrders),
                        2,
                        RoundingMode.HALF_UP
                )
                        : BigDecimal.ZERO
        );


        return summary;
    }
}