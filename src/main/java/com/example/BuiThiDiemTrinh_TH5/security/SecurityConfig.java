package com.example.BuiThiDiemTrinh_TH5.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            ObjectMapper objectMapper,
            @Value("${app.security.cookie-secure:false}") boolean secureCookie
    ) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .sameSite("Lax")
                .secure(secureCookie));

        http
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .requestCache(cache -> cache.requestCache(new org.springframework.security.web.savedrequest.NullRequestCache()))
                .exceptionHandling(errors -> errors
                        .defaultAuthenticationEntryPointFor(
                                (request, response, exception) -> writeSecurityError(response, objectMapper, HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập"),
                                new AntPathRequestMatcher("/api/**"))
                        .defaultAccessDeniedHandlerFor(
                                (request, response, exception) -> writeSecurityError(response, objectMapper, HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
                                new AntPathRequestMatcher("/api/**")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/csrf", "/api/auth/login").permitAll()
                        .requestMatchers("/api/guest/qr/*", "/api/guest/qr/*/orders").permitAll()
                        .requestMatchers("/api/users/**", "/api/employees/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/tables/**").authenticated()
                        .requestMatchers("/api/products/**", "/api/categories/**", "/api/tables/**").hasRole("ADMIN")
                        .requestMatchers("/api/customers/**").hasAnyRole("ADMIN", "CASHIER", "STAFF")
                        .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "CASHIER", "STAFF")
                        .requestMatchers("/api/qr/**", "/api/inventory/**", "/api/reservations/**",
                                "/api/shifts/**", "/api/suppliers/**", "/api/expenses/**", "/api/promotions/**", "/api/rewards/**")
                                .hasAnyRole("ADMIN", "CASHIER", "STAFF")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN"));
        return http.build();
    }

    private static void writeSecurityError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getOutputStream(), new SecurityError(status.value(), message));
    }

    private record SecurityError(int status, String message) { }
}
