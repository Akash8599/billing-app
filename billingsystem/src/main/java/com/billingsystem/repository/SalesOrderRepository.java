package com.billingsystem.repository;


import com.billingsystem.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    /**
     * Find order by order number
     */
    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    /**
     * Find all orders for a customer by phone number
     */
    List<SalesOrder> findByCustomerPhone(String customerPhone);

    /**
     * Find all orders for a customer by email
     */
    List<SalesOrder> findByCustomerEmail(String customerEmail);

    /**
     * Find all orders for a customer by name (case-insensitive)
     */
    List<SalesOrder> findByCustomerNameIgnoreCase(String customerName);

    /**
     * Find all unpaid orders
     */
    @Query("SELECT so FROM SalesOrder so WHERE so.paymentStatus = 'UNPAID'")
    List<SalesOrder> findUnpaidOrders();

    /**
     * Find all paid orders
     */
    @Query("SELECT so FROM SalesOrder so WHERE so.paymentStatus = 'PAID'")
    List<SalesOrder> findPaidOrders();

    /**
     * Find all cancelled orders
     */
    @Query("SELECT so FROM SalesOrder so WHERE so.status = 'CANCELLED'")
    List<SalesOrder> findCancelledOrders();

    /**
     * Find orders created between two dates
     */
    @Query("SELECT so FROM SalesOrder so WHERE so.createdAt BETWEEN :startDate AND :endDate")
    List<SalesOrder> findOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count orders by status
     */
    long countByStatus(SalesOrder.OrderStatus status);

    /**
     * Count orders by payment status
     */
    long countByPaymentStatus(SalesOrder.PaymentStatus paymentStatus);

    /**
     * Find orders by created by user
     */
    List<SalesOrder> findByCreatedBy(String createdBy);

    /**
     * Search orders by customer name or phone
     */
    @Query("SELECT so FROM SalesOrder so WHERE " +
            "LOWER(so.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "so.customerPhone LIKE CONCAT('%', :searchTerm, '%')")
    List<SalesOrder> searchOrders(@Param("searchTerm") String searchTerm);
}