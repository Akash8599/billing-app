package com.billingsystem.repository;

import com.billingsystem.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Get PO by number for logged-in user
    Optional<PurchaseOrder> findByPoNumberAndCreatedBy(String poNumber, Long createdBy);

    // Get all POs by status for logged-in user
    List<PurchaseOrder> findByStatusAndCreatedBy(String status, Long createdBy);

    // Get PO by ID for logged-in user
    Optional<PurchaseOrder> findByIdAndCreatedBy(Long id, Long createdBy);

    // Get all POs for logged-in user
    List<PurchaseOrder> findAllByCreatedBy(Long createdBy);
}
