package com.billingsystem.service;

import com.billingsystem.dto.CustomerRequest;
import com.billingsystem.mapper.CustomerMapper;
import com.billingsystem.model.Customer;
import com.billingsystem.repository.CustomerRepository;
import com.billingsystem.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * Create new customer
     */
    public CustomerRequest createOrUpdateCustomer(CustomerRequest req) {

        if (req.getPhone() == null || req.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        Customer customer = customerRepository
                .findByPhoneNumberAndCreatedBy(req.getPhone(), SecurityUtils.currentUserId())
                .orElseGet(() -> {
                    Customer c = customerMapper.toCustomer(req);
                    c.setCreatedAt(System.currentTimeMillis());
                    c.setCreatedBy(SecurityUtils.currentUserId());
                    return c;
                });

        // update existing or newly created entity IN-PLACE
        customerMapper.updateCustomerFromRequest(req, customer);

        customer.setUpdatedAt(System.currentTimeMillis());
        customer.setUpdatedBy(SecurityUtils.currentUserId());

        Customer saved = customerRepository.save(customer);
        return customerMapper.toCustomerRequest(saved);
    }


    /**
     * Get all customers
     */
    public List<CustomerRequest> getAllCustomers() {
        List<Customer> customers = customerRepository.findAllByCreatedBy(SecurityUtils.currentUserId());
        List<CustomerRequest> collect = customers.stream().map(customer -> customerMapper
                .toCustomerRequest(customer)).collect(Collectors.toList());
        return collect;
    }

    /**
     * Get customer by ID
     */
    public Optional<CustomerRequest> getCustomerById(Long id) {
        Optional<Customer> byId = customerRepository.findById(id);
        return byId.map(customer -> customerMapper.toCustomerRequest(customer));
    }

    /**
     * Get or create customer by phone number (for quick checkout)
     */
    public Customer getOrCreateCustomer(String phoneNumber, String name, String type) {
        Optional<Customer> existing = customerRepository.findByPhoneNumberAndCreatedBy(phoneNumber, SecurityUtils.currentUserId());
        
        if (existing.isPresent()) {
            return existing.get();
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhoneNumber(phoneNumber);
//        customer.setCustomerType(type);
//        customer.setGstNumber("");
        customer.setCreatedAt(System.currentTimeMillis());
        customer.setUpdatedAt(System.currentTimeMillis());
        customer.setCreatedBy(SecurityUtils.currentUserId());
        customer.setUpdatedBy(SecurityUtils.currentUserId());
        
        return customerRepository.save(customer);
    }

    /**
     * Update customer
     */
    public CustomerRequest updateCustomer(Long id, CustomerRequest customerData) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setName(customerData.getName());
        customer.setPhoneNumber(customerData.getPhone());
        customer.setEmail(customerData.getEmail());
        customer.setAddress(customerData.getAddress());
        customer.setGstNumber(customerData.getGstin());
        customer.setState(customerData.getState());
//        customer.setStateCode(customerData.getState());
//        customer.setCustomerType(customerData.getCustomerType());
        customer.setUpdatedAt(System.currentTimeMillis());
        customer.setUpdatedBy(SecurityUtils.currentUserId());

        Customer update = customerRepository.save(customer);
        CustomerRequest  response = customerMapper.toCustomerRequest(update);
        return response;
    }

    /**
     * Delete customer
     */
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
