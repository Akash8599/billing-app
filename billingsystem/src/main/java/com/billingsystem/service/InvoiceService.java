package com.billingsystem.service;

import com.billingsystem.dto.CreateInvoiceRequest;
import com.billingsystem.dto.DashboardStatsResponse;
import com.billingsystem.model.*;
import com.billingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final GSTService gstService;

    /**
     * Create new sales invoice and automatically deduct inventory
     */
    public Invoice createInvoice(CreateInvoiceRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<InvoiceItem> items = new ArrayList<>();
        Double subtotal = 0.0;
        Double totalTax = 0.0;

        for (CreateInvoiceRequest.InvoiceItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));

            // Check stock availability
            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getName() + 
                        ". Available: " + product.getQuantity() + ", Requested: " + itemReq.getQuantity());
            }

            // Calculate item total with tax
            Double itemSubtotal = product.getSellingPrice() * itemReq.getQuantity();
            Double itemTax = gstService.calculateTax(itemSubtotal, product.getGstRate());
            Double itemTotal = itemSubtotal + itemTax;

            InvoiceItem item = new InvoiceItem();
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(product.getSellingPrice());
            item.setGstRate(product.getGstRate());
            item.setItemTax(itemTax);
            item.setItemTotal(itemTotal);

            items.add(item);
            subtotal += itemSubtotal;
            totalTax += itemTax;

            // Reduce inventory
            product.setQuantity(product.getQuantity() - itemReq.getQuantity());
            product.setUpdatedAt(System.currentTimeMillis());
            productRepository.save(product);
        }

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setCustomer(customer);
        invoice.setItems(items);
        invoice.setSubtotal(subtotal);
        invoice.setTotalTax(totalTax);
        invoice.setTotalAmount(subtotal + totalTax);
        invoice.setInvoiceType(gstService.getInvoiceType(customer.getCustomerType()));
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setStatus("COMPLETED");
        invoice.setInvoiceDate(System.currentTimeMillis());
        invoice.setCreatedAt(System.currentTimeMillis());

        return invoiceRepository.save(invoice);
    }

    /**
     * Get all invoices
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    /**
     * Get invoice by ID
     */
    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    /**
     * Get today's sales summary
     */
    public DashboardStatsResponse getDashboardStats() {
        long startOfDay = Instant.now().getEpochSecond() * 1000;
        startOfDay = startOfDay - (startOfDay % (24 * 60 * 60 * 1000));

        long endOfDay = startOfDay + (24 * 60 * 60 * 1000);

        List<Invoice> todaysInvoices = invoiceRepository.findByDateRange(startOfDay, endOfDay);
        Double todaysSales = todaysInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        List<DashboardStatsResponse.LowStockItem> lowStockItems = lowStockProducts.stream()
                .map(p -> new DashboardStatsResponse.LowStockItem(
                        p.getId(),
                        p.getSku(),
                        p.getName(),
                        p.getQuantity(),
                        p.getLowStockAlert()
                ))
                .collect(Collectors.toList());

        List<Invoice> allInvoices = invoiceRepository.findCompletedInvoices();
        Double totalRevenue = allInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        return new DashboardStatsResponse(
                todaysSales,
                todaysInvoices.size(),
                lowStockProducts.size(),
                totalRevenue,
                lowStockItems
        );
    }

    /**
     * Generate unique invoice number
     */
    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    /**
     * Cancel invoice (reverse stock)
     */
    public void cancelInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if ("CANCELLED".equals(invoice.getStatus())) {
            throw new RuntimeException("Invoice already cancelled");
        }

        // Reverse inventory
        for (InvoiceItem item : invoice.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            product.setUpdatedAt(System.currentTimeMillis());
            productRepository.save(product);
        }

        invoice.setStatus("CANCELLED");
        invoiceRepository.save(invoice);
    }
}
