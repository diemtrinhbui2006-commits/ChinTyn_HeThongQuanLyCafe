package com.example.BuiThiDiemTrinh_TH5.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BuiThiDiemTrinh_TH5.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
