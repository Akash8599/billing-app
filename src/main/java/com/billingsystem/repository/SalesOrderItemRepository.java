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
     * ✅ FIXED: Find all sales order items that use a specific product
     * Using product.id instead of productId (which is @Transient)
     *
     * @param productId Product ID to search for
     * @return List of SalesOrderItem that reference this product
     */
    @Query("SELECT soi FROM SalesOrderItem soi WHERE soi.product.id = :productId")
    List<SalesOrderItem> findByProductId(@Param("productId") Long productId);

    /**
     * ✅ FIXED: Count how many sales order items use a specific product
     *
     * @param productId Product ID to count
     * @return Number of items using this product
     */
    @Query("SELECT COUNT(soi) FROM SalesOrderItem soi WHERE soi.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);

    /**
     * Find all items in a specific sales order
     *
     * @param salesOrderId Sales order ID
     * @return List of items in that order
     */
    List<SalesOrderItem> findBySalesOrderId(Long salesOrderId);
}