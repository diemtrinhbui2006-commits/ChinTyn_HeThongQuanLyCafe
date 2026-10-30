package com.example.BuiThiDiemTrinh_TH5.security;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.BuiThiDiemTrinh_TH5.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Rechecks server-side roles and revokes sessions when their saved password changes. */
public class SessionAccountValidationFilter extends OncePerRequestFilter {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final SecurityContextRepository contexts;
    public SessionAccountValidationFilter(UserRepository users, PasswordEncoder passwords, SecurityContextRepository contexts) {
        this.users = users; this.passwords = passwords; this.contexts = contexts;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            var session = request.getSession(false);
            var account = users.findByUsernameIgnoreCase(auth.getName());
            Object savedPassword = session == null ? null : session.getAttribute("accountPasswordVersion");
            if ((account.isEmpty() && savedPassword != null) || (account.isPresent() && savedPassword != null && !savedPassword.equals(account.get().getPassword()))) {
                if (session != null) session.invalidate();
                SecurityContextHolder.clearContext();
            } else if (account.isPresent()) {
                String role = account.get().getRole().trim().toUpperCase(Locale.ROOT);
                if (!List.of("ADMIN", "CASHIER", "STAFF").contains(role)) {
                    if (session != null) session.invalidate();
                    SecurityContextHolder.clearContext();
                } else {
                    if (role.equals("ADMIN") && passwords.matches("123456", account.get().getPassword())) role = "PASSWORD_CHANGE_REQUIRED";
                    String authority = "ROLE_" + role;
                    if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals(authority))) {
                        var updated = UsernamePasswordAuthenticationToken.authenticated(auth.getPrincipal(), null, List.of(new SimpleGrantedAuthority(authority)));
                        var context = SecurityContextHolder.createEmptyContext(); context.setAuthentication(updated);
                        SecurityContextHolder.setContext(context); contexts.saveContext(context, request, response);
                    }
                    if (session != null && savedPassword == null) session.setAttribute("accountPasswordVersion", account.get().getPassword());
                }
            }
        }
        chain.doFilter(request, response);
    }
}
