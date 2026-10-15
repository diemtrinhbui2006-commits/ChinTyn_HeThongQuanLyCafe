package com.example.BuiThiDiemTrinh_TH5.service;

import java.util.List;

import com.example.BuiThiDiemTrinh_TH5.entity.Customer;

public interface CustomerService {

    Customer create(Customer customer);

    List<Customer> getAllCustomers();

    Customer getCustomerById(Long id);

    Customer update(Long id, Customer customer);

    void delete(Long id);
}
