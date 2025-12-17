package com.billingsystem.service;

import com.billingsystem.dto.CustomerRequest;
import com.billingsystem.mapper.CustomerMapper;
import com.billingsystem.model.Customer;
import com.billingsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public CustomerRequest createCustomer(CustomerRequest customerRequest) {
        customerRequest.setCreatedAt(System.currentTimeMillis());
        customerRequest.setUpdatedAt(System.currentTimeMillis());

        Customer customer = customerMapper.toCustomer(customerRequest);
        Customer save = customerRepository.save(customer);
        CustomerRequest  response = customerMapper.toCustomerRequest(save);
        return response;
    }

    /**
     * Get all customers
     */
    public List<CustomerRequest> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream().map(customer -> customerMapper.toCustomerRequest(customer)).collect(Collectors.toList());
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
        Optional<Customer> existing = customerRepository.findByPhoneNumber(phoneNumber);
        
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
