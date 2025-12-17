package com.billingsystem.mapper;


import com.billingsystem.dto.CustomerRequest;
import com.billingsystem.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface CustomerMapper {

    @Mapping(source = "phone", target = "phoneNumber")
    @Mapping(source = "gstin", target = "gstNumber")
    public Customer toCustomer(CustomerRequest customerRequest);

    @Mapping(source = "phoneNumber", target = "phone")
    @Mapping(source = "gstNumber", target = "gstin")
    public CustomerRequest toCustomerRequest(Customer customerRequest);
}
