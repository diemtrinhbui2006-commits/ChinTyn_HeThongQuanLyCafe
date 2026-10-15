package com.example.BuiThiDiemTrinh_TH5.service.Impl;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.entity.Employee;
import com.example.BuiThiDiemTrinh_TH5.entity.User;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.exception.ResourceNotFoundException;
import com.example.BuiThiDiemTrinh_TH5.repository.EmployeeRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.UserRepository;
import com.example.BuiThiDiemTrinh_TH5.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserServiceImpl(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public User create(User user) {
        validate(user);
        user.setId(null);
        user.setEmployee(resolveEmployee(user.getEmployee()));
        user.setPassword(hashPassword(user.getPassword()));
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
        existing.setPassword(hashPassword(user.getPassword()));
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
        if (user.getRole() == null || user.getRole().isBlank()) {
            throw new BadRequestException("Vai trò không được để trống");
        }
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            return "pbkdf2$" + ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException("Không thể mã hóa mật khẩu", exception);
        } finally {
            spec.clearPassword();
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
