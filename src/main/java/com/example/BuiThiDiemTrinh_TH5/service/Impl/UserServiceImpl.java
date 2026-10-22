package com.example.BuiThiDiemTrinh_TH5.service.Impl;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.BuiThiDiemTrinh_TH5.entity.Employee;
import com.example.BuiThiDiemTrinh_TH5.entity.User;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.exception.ResourceNotFoundException;
import com.example.BuiThiDiemTrinh_TH5.repository.EmployeeRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.UserRepository;
import com.example.BuiThiDiemTrinh_TH5.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "CASHIER", "STAFF");

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User create(User user) {
        validate(user);
        user.setId(null);
        user.setEmployee(resolveEmployee(user.getEmployee()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user có id = " + id));
    }

    @Override
    @Transactional
    public User update(Long id, User user) {
        validate(user);
        User existing = getUserById(id);
        existing.setUsername(user.getUsername());
        existing.setRole(user.getRole());
        existing.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getEmployee() != null) {
            existing.setEmployee(resolveEmployee(user.getEmployee()));
        }
        return userRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(getUserById(id));
    }

    private void validate(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            throw new BadRequestException("Tên đăng nhập không được để trống");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException("Mật khẩu không được để trống");
        }
        if (user.getPassword().length() < 8) {
            throw new BadRequestException("Mật khẩu phải có ít nhất 8 ký tự");
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            throw new BadRequestException("Vai trò không được để trống");
        }
        user.setRole(user.getRole().trim().toUpperCase(Locale.ROOT));
        if (!ALLOWED_ROLES.contains(user.getRole())) {
            throw new BadRequestException("Vai trò chỉ được là ADMIN, CASHIER hoặc STAFF");
        }
    }

    private Employee resolveEmployee(Employee employee) {
        if (employee == null) {
            return null;
        }
        if (employee.getId() == null) {
            throw new BadRequestException("Mã nhân viên không hợp lệ");
        }
        return employeeRepository.findById(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhân viên có id = " + employee.getId()
                ));
    }
}
