package com.billingsystem.service;

import com.billingsystem.dto.CreatePurchaseOrderRequest;
import com.billingsystem.model.*;
import com.billingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {
    private final PurchaseOrderRepository poRepository;
    private final ProductRepository productRepository;

    /**
     * Create new purchase order
     */
    public PurchaseOrder createPurchaseOrder(CreatePurchaseOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Purchase order must contain at least one item");
        }

        List<PurchaseOrderItem> items = new ArrayList<>();
        Double totalAmount = 0.0;

        for (CreatePurchaseOrderRequest.PurchaseOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));

            Double itemTotal = itemReq.getQuantity() * itemReq.getCostPrice();

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setCostPrice(itemReq.getCostPrice());
            item.setItemTotal(itemTotal);

            items.add(item);
            totalAmount += itemTotal;
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePONumber());
        po.setSupplierName(request.getSupplierName());
        po.setItems(items);
        po.setTotalAmount(totalAmount);
        po.setStatus("ORDERED");
        po.setOrderDate(System.currentTimeMillis());
        po.setCreatedAt(System.currentTimeMillis());

        return poRepository.save(po);
    }

    /**
     * Receive purchase order (add inventory)
     */
    public PurchaseOrder receivePurchaseOrder(Long poId) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if ("RECEIVED".equals(po.getStatus())) {
            throw new RuntimeException("Purchase order already received");
        }

        if ("CANCELLED".equals(po.getStatus())) {
            throw new RuntimeException("Cannot receive cancelled purchase order");
        }

        // Add inventory for all items
        for (PurchaseOrderItem item : po.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            // Update cost price from this PO
            product.setCostPrice(item.getCostPrice());
            product.setUpdatedAt(System.currentTimeMillis());
            productRepository.save(product);
        }

        po.setStatus("RECEIVED");
        po.setReceivedDate(System.currentTimeMillis());
        return poRepository.save(po);
    }

    /**
     * Get all purchase orders
     */
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return poRepository.findAll();
    }

    /**
     * Get purchase order by ID
     */
    public Optional<PurchaseOrder> getPurchaseOrderById(Long id) {
        return poRepository.findById(id);
    }

    /**
     * Get pending purchase orders
     */
    public List<PurchaseOrder> getPendingPurchaseOrders() {
        return poRepository.findByStatus("ORDERED");
    }

    /**
     * Get received purchase orders
     */
    public List<PurchaseOrder> getReceivedPurchaseOrders() {
        return poRepository.findByStatus("RECEIVED");
    }

    /**
     * Cancel purchase order
     */
    public void cancelPurchaseOrder(Long poId) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if ("RECEIVED".equals(po.getStatus())) {
            throw new RuntimeException("Cannot cancel received purchase order");
        }

        po.setStatus("CANCELLED");
        poRepository.save(po);
    }

    /**
     * Generate unique PO number
     */
    private String generatePONumber() {
        return "PO-" + System.currentTimeMillis();
    }
}
