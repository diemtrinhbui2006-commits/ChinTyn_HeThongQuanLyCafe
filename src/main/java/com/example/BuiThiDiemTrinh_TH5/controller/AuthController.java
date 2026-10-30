package com.example.BuiThiDiemTrinh_TH5.controller;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BuiThiDiemTrinh_TH5.dto.AuthenticatedUserResponse;
import com.example.BuiThiDiemTrinh_TH5.dto.LoginRequest;
import com.example.BuiThiDiemTrinh_TH5.entity.User;
import com.example.BuiThiDiemTrinh_TH5.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        if (request == null || request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(new AuthError("Vui lòng nhập tên đăng nhập và mật khẩu"));
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username().trim(), request.password()));
            new ChangeSessionIdAuthenticationStrategy().onAuthentication(authentication, servletRequest, servletResponse);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, servletRequest, servletResponse);
            var account = userRepository.findByUsernameIgnoreCase(authentication.getName()).orElseThrow();
            servletRequest.getSession().setAttribute("accountPasswordVersion", account.getPassword());
            return ResponseEntity.ok(toResponse(authentication));
        } catch (BadCredentialsException exception) {
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthError("Tên đăng nhập hoặc mật khẩu không đúng"));
        }
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse currentUser(Authentication authentication) {
        return toResponse(authentication);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest input, Authentication authentication,
            HttpServletRequest request) {
        var account = userRepository.findByUsernameIgnoreCase(authentication.getName()).orElseThrow();
        if (input.oldPassword() == null || !passwordEncoder.matches(input.oldPassword(), account.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthError("Mật khẩu hiện tại không đúng"));
        }
        if (input.newPassword() == null || input.newPassword().length() < 12 || input.newPassword().length() > 128
                || input.newPassword().equals(input.oldPassword()) || input.newPassword().isBlank()) {
            return ResponseEntity.badRequest().body(new AuthError("Mật khẩu mới phải có 12 đến 128 ký tự và khác mật khẩu cũ"));
        }
        account.setPassword(passwordEncoder.encode(input.newPassword()));
        userRepository.save(account);
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    public record PasswordChangeRequest(String oldPassword, String newPassword) { }

    private AuthenticatedUserResponse toResponse(Authentication authentication) {
        User account = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("Tài khoản không còn tồn tại"));
        String role = account.getRole().toUpperCase(Locale.ROOT);
        boolean mustChange = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PASSWORD_CHANGE_REQUIRED"));
        String name = account.getEmployee() == null ? account.getUsername() : account.getEmployee().getName();
        Long employeeId = account.getEmployee() == null ? null : account.getEmployee().getId();
        return new AuthenticatedUserResponse(account.getId(), account.getUsername(), name, role, employeeId, mustChange);
    }

    private record AuthError(String message) { }
}
