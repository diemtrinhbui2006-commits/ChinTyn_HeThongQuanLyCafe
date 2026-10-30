package com.example.BuiThiDiemTrinh_TH5.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.example.BuiThiDiemTrinh_TH5.repository.UserRepository;

@Component
public class EmployeeAccessPolicy {
    private final UserRepository users;
    public EmployeeAccessPolicy(UserRepository users) { this.users = users; }
    public boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    public Long employeeId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("Vui lòng đăng nhập");
        var account = users.findByUsernameIgnoreCase(auth.getName()).orElseThrow(() -> new AccessDeniedException("Tài khoản không tồn tại"));
        if (account.getEmployee() == null) throw new AccessDeniedException("Tài khoản chưa gắn nhân viên");
        return account.getEmployee().getId();
    }
    public void requireOwnEmployee(Long id) {
        if (!isAdmin() && !employeeId().equals(id)) throw new AccessDeniedException("Bạn chỉ được thao tác ca của mình");
    }
}
