package com.billingsystem.repository;

import com.billingsystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Find a customer by phone number **for a specific user**
    Optional<Customer> findByPhoneNumberAndCreatedBy(String phoneNumber, Long createdBy);

    // Optional: find all customers for a user
    List<Customer> findAllByCreatedBy(Long createdBy);
}
