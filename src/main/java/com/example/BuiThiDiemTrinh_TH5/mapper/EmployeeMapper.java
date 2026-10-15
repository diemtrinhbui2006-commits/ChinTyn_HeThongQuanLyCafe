package com.example.BuiThiDiemTrinh_TH5.mapper;

import org.springframework.stereotype.Component;

import com.example.BuiThiDiemTrinh_TH5.dto.EmployeeRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.EmployeeResponse;
import com.example.BuiThiDiemTrinh_TH5.entity.Employee;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        if (request == null) {
            return null;
        }
        return new Employee(
                null,
                request.name(),
                request.email(),
                request.phone(),
                request.position(),
                request.salary()
        );
    }

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getPosition(),
                employee.getSalary()
        );
    }
}
