package com.example.BuiThiDiemTrinh_TH5;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.entity.User;
import com.example.BuiThiDiemTrinh_TH5.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createCashierAccount() {
        User user = new User();
        user.setUsername("security-cashier");
        user.setPassword(passwordEncoder.encode("secure-pass-2026"));
        user.setRole("CASHIER");
        userRepository.save(user);
    }

    @Test
    void protectedApiRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Vui lòng đăng nhập"));
    }

    @Test
    void loginCreatesServerSessionAndReturnsDatabaseRole() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"security-cashier\",\"password\":\"secure-pass-2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("security-cashier"))
                .andExpect(jsonPath("$.role").value("CASHIER"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CASHIER"));
        mockMvc.perform(get("/api/users").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidCredentialsDoNotAuthenticate() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"security-cashier\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CASHIER")
    void unsafeRequestRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/api/invoices/1/cancel"))
                .andExpect(status().isForbidden());
    }

    @Test
    void passwordEncoderAcceptsExistingSeedHash() {
        String seedHash = "pbkdf2$120000$BlSayiU+vb3TgzhMkdPreQ==$QHGBSgDtiLE4Z3HkMHvs1yJypc66q6TTC7A5QGJdnYw=";
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("123456", seedHash));
        org.junit.jupiter.api.Assertions.assertFalse(passwordEncoder.matches("wrong", seedHash));
    }
}
