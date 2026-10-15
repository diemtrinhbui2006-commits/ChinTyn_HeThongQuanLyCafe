package com.example.BuiThiDiemTrinh_TH5.service;

import java.util.List;

import com.example.BuiThiDiemTrinh_TH5.entity.Employee;

public interface EmployeeService {

    Employee create(Employee employee);

    List<Employee> getAllEmployees();

    Employee getEmployeeById(Long id);

    Employee update(Long id, Employee employee);

    void delete(Long id);
}
