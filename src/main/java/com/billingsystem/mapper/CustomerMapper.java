package com.billingsystem.mapper;


import com.billingsystem.dto.CustomerRequest;
import com.billingsystem.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface CustomerMapper {

    @Mapping(source = "phone", target = "phoneNumber")
    @Mapping(source = "gstin", target = "gstNumber")
    @Mapping(source = "statecode", target = "stateCode")
    @Mapping(source = "pincode", target = "pinCode")
    public Customer toCustomer(CustomerRequest customerRequest);

    @Mapping(source = "phoneNumber", target = "phone")
    @Mapping(source = "gstNumber", target = "gstin")
    @Mapping(source = "stateCode", target = "statecode")
    @Mapping(source = "pinCode", target = "pincode")
    public CustomerRequest toCustomerRequest(Customer customerRequest);
}
