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

    /* ===============================
       BASIC LOOKUPS (USER SCOPED)
       =============================== */

    Optional<SalesOrder> findByOrderNumberAndCreatedBy(String orderNumber, Long createdBy);

    List<SalesOrder> findAllByCreatedBy(Long createdBy);

    Optional<SalesOrder> findByIdAndCreatedBy(Long id, Long createdBy);

    /* ===============================
       CUSTOMER FILTERS (USER SCOPED)
       =============================== */

    List<SalesOrder> findByCustomerPhoneAndCreatedBy(String customerPhone, Long createdBy);

    List<SalesOrder> findByCustomerEmailAndCreatedBy(String customerEmail, Long createdBy);

    List<SalesOrder> findByCustomerNameIgnoreCaseAndCreatedBy(String customerName, Long createdBy);

    /* ===============================
       STATUS FILTERS (USER SCOPED)
       =============================== */

    List<SalesOrder> findByPaymentStatusAndCreatedBy(
            SalesOrder.PaymentStatus paymentStatus,
            Long createdBy
    );

    List<SalesOrder> findByStatusAndCreatedBy(
            SalesOrder.OrderStatus status,
            Long createdBy
    );

    /* ===============================
       DATE RANGE (USER SCOPED)
       =============================== */

    @Query("""
        SELECT so
        FROM SalesOrder so
        WHERE so.createdAt BETWEEN :startDate AND :endDate
          AND so.createdBy = :userId
    """)
    List<SalesOrder> findOrdersBetweenDatesAndUser(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("userId") Long userId
    );

    /* ===============================
       COUNTS (USER SCOPED)
       =============================== */

    long countByStatusAndCreatedBy(
            SalesOrder.OrderStatus status,
            Long createdBy
    );

    long countByPaymentStatusAndCreatedBy(
            SalesOrder.PaymentStatus paymentStatus,
            Long createdBy
    );

    /* ===============================
       SEARCH (USER SCOPED)
       =============================== */

    @Query("""
        SELECT so
        FROM SalesOrder so
        WHERE so.createdBy = :userId
          AND (
              LOWER(so.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
              OR so.customerPhone LIKE CONCAT('%', :searchTerm, '%')
          )
    """)
    List<SalesOrder> searchOrdersByUser(
            @Param("searchTerm") String searchTerm,
            @Param("userId") Long userId
    );
}
