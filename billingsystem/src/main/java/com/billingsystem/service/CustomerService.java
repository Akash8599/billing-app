package com.billingsystem.service;

import com.billingsystem.model.Customer;
import com.billingsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;

    /**
     * Create new customer
     */
    public Customer createCustomer(Customer customer) {
        customer.setCreatedAt(System.currentTimeMillis());
        customer.setUpdatedAt(System.currentTimeMillis());
        return customerRepository.save(customer);
    }

    /**
     * Get all customers
     */
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Get customer by ID
     */
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    /**
     * Get or create customer by phone number (for quick checkout)
     */
    public Customer getOrCreateCustomer(String phoneNumber, String name, String type) {
        Optional<Customer> existing = customerRepository.findByPhoneNumber(phoneNumber);
        
        if (existing.isPresent()) {
            return existing.get();
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhoneNumber(phoneNumber);
        customer.setCustomerType(type);
        customer.setGstNumber("");
        customer.setCreatedAt(System.currentTimeMillis());
        customer.setUpdatedAt(System.currentTimeMillis());
        
        return customerRepository.save(customer);
    }

    /**
     * Update customer
     */
    public Customer updateCustomer(Long id, Customer customerData) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setName(customerData.getName());
        customer.setPhoneNumber(customerData.getPhoneNumber());
        customer.setEmail(customerData.getEmail());
        customer.setAddress(customerData.getAddress());
        customer.setGstNumber(customerData.getGstNumber());
        customer.setCustomerType(customerData.getCustomerType());
        customer.setUpdatedAt(System.currentTimeMillis());

        return customerRepository.save(customer);
    }

    /**
     * Delete customer
     */
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
