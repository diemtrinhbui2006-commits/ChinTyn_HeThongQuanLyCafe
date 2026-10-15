package com.example.BuiThiDiemTrinh_TH5.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.entity.Employee;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.exception.ResourceNotFoundException;
import com.example.BuiThiDiemTrinh_TH5.repository.EmployeeRepository;
import com.example.BuiThiDiemTrinh_TH5.service.EmployeeService;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee create(Employee employee) {
        validate(employee);
        employee.setId(null);
        return employeeRepository.save(employee);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên có id = " + id));
    }

    @Override
    @Transactional
    public Employee update(Long id, Employee employee) {
        validate(employee);
        Employee existing = getEmployeeById(id);
        existing.setName(employee.getName());
        existing.setEmail(employee.getEmail());
        existing.setPhone(employee.getPhone());
        existing.setPosition(employee.getPosition());
        existing.setSalary(employee.getSalary());
        return employeeRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        employeeRepository.delete(getEmployeeById(id));
    }

    private void validate(Employee employee) {
        if (employee == null || employee.getName() == null || employee.getName().isBlank()) {
            throw new BadRequestException("Tên nhân viên không được để trống");
        }
        if (employee.getPosition() == null || employee.getPosition().isBlank()) {
            throw new BadRequestException("Chức vụ nhân viên không được để trống");
        }
        if (employee.getSalary() == null || !Double.isFinite(employee.getSalary()) || employee.getSalary() < 0) {
            throw new BadRequestException("Lương nhân viên không hợp lệ");
        }
    }
}
