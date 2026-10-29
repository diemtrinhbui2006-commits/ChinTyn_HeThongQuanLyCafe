package com.example.BuiThiDiemTrinh_TH5.mapper;

import org.springframework.stereotype.Component;

import com.example.BuiThiDiemTrinh_TH5.dto.CustomerRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.CustomerResponse;
import com.example.BuiThiDiemTrinh_TH5.entity.Customer;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        if (request == null) {
            return null;
        }
        return new Customer(null, request.name(), request.email(), request.phone(), request.address(), 0);
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints()
        );
    }
}
