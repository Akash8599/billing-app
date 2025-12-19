package com.billingsystem.repository;

import com.billingsystem.model.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    /**
     * Find sales order items using a product
     * scoped to logged-in user
     */
    @Query("""
        SELECT soi
        FROM SalesOrderItem soi
        WHERE soi.product.id = :productId
          AND soi.salesOrder.createdBy = :userId
    """)
    List<SalesOrderItem> findByProductIdAndUser(
            @Param("productId") Long productId,
            @Param("userId") Long userId
    );

    /**
     * Count product usage for logged-in user
     */
    @Query("""
        SELECT COUNT(soi)
        FROM SalesOrderItem soi
        WHERE soi.product.id = :productId
          AND soi.salesOrder.createdBy = :userId
    """)
    long countByProductIdAndUser(
            @Param("productId") Long productId,
            @Param("userId") Long userId
    );

    /**
     * Get all items in a sales order (user-safe)
     */
    List<SalesOrderItem> findBySalesOrderIdAndSalesOrderCreatedBy(
            Long salesOrderId,
            Long userId
    );
}
