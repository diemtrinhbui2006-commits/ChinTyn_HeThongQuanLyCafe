package com.example.BuiThiDiemTrinh_TH5.security;

import java.util.Locale;
import java.util.Set;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.BuiThiDiemTrinh_TH5.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "CASHIER", "STAFF");

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserDetailsServiceImpl(UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var account = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tên đăng nhập hoặc mật khẩu không đúng"));
        String role = account.getRole() == null ? "" : account.getRole().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(role)) {
            throw new UsernameNotFoundException("Tài khoản không có vai trò hợp lệ");
        }
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(role.equals("ADMIN") && passwordEncoder.matches("123456", account.getPassword()) ? "PASSWORD_CHANGE_REQUIRED" : role)
                .build();
    }
}
