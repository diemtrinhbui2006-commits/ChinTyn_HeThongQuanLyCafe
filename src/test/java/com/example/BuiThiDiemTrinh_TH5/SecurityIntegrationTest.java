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
    @Autowired private com.example.BuiThiDiemTrinh_TH5.repository.ProductRepository products;
    @Autowired private com.example.BuiThiDiemTrinh_TH5.repository.CategoryRepository categories;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDeleteProductReturns204AndProductDisappears() throws Exception {
        var category = new com.example.BuiThiDiemTrinh_TH5.entity.Category(); category.setName("Delete test category"); category = categories.save(category);
        var product = new com.example.BuiThiDiemTrinh_TH5.entity.Product(null, "Delete test product", "", new java.math.BigDecimal("25000"), 10, true, category);
        product = products.save(product);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/products/" + product.getId()).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/products/" + product.getId())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/products/search").param("keyword", "Delete test product"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }
    @Test
    void existingSessionLosesAdminRightsAfterDatabaseRoleChangeAndPasswordReset() throws Exception {
        User admin = new User(); admin.setUsername("revocation-admin"); admin.setRole("ADMIN");
        admin.setPassword(passwordEncoder.encode("unique-secure-admin-pass")); admin = userRepository.save(admin);
        var logged = mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"revocation-admin\",\"password\":\"unique-secure-admin-pass\"}"))
                .andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) logged.getRequest().getSession(false);
        mockMvc.perform(get("/api/users").session(session)).andExpect(status().isOk());
        admin.setRole("CASHIER"); userRepository.saveAndFlush(admin);
        mockMvc.perform(get("/api/users").session(session)).andExpect(status().isForbidden());
        admin.setPassword(passwordEncoder.encode("another-secure-password")); userRepository.saveAndFlush(admin);
        mockMvc.perform(get("/api/products").session(session)).andExpect(status().isUnauthorized());
    }
    @Test
    void staffAndCashierCannotMutateAdministrativeDataEvenWithValidCsrf() throws Exception {
        for (String role : new String[] {"STAFF", "CASHIER"}) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/products/1")
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("operator").roles(role)).with(csrf()))
                    .andExpect(status().isForbidden());
            for (String path : new String[] {"/api/users", "/api/employees", "/api/products", "/api/categories", "/api/tables",
                    "/api/customers", "/api/promotions", "/api/rewards", "/api/suppliers", "/api/expenses", "/api/inventory/ingredients"}) {
                mockMvc.perform(post(path).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("operator").roles(role))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isForbidden());
            }
            mockMvc.perform(get("/users").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("operator").roles(role)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/products/1")
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("operator").roles(role))
                    .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")) .andExpect(status().isForbidden());
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/customers/1")
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("operator").roles(role)).with(csrf()))
                    .andExpect(status().isForbidden());
        }
        mockMvc.perform(post("/api/invoices/1/checkout").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("operator").roles("STAFF"))
                .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/invoices/1/discount").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("operator").roles("CASHIER"))
                .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateUpdateAndDeleteUser() throws Exception {
        var created = mockMvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"managed-staff\",\"password\":\"strong-test-pass\",\"role\":\"STAFF\"}"))
                .andExpect(status().isOk()).andReturn();
        long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(created.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/users/" + id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"managed-cashier\",\"password\":\"updated-test-pass\",\"role\":\"CASHIER\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("CASHIER"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/users/" + id).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void defaultAdminMustChangePasswordBeforeAccessingAdminApi() throws Exception {
        User admin = new User(); admin.setUsername("default-admin-test"); admin.setRole("ADMIN");
        admin.setPassword(passwordEncoder.encode("123456")); userRepository.save(admin);
        var logged = mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"default-admin-test\",\"password\":\"123456\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.passwordChangeRequired").value(true)).andReturn();
        var session = (MockHttpSession) logged.getRequest().getSession(false);
        mockMvc.perform(get("/api/users").session(session)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/products").session(session)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/auth/change-password").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"oldPassword\":\"123456\",\"newPassword\":\"new-admin-test-pass-2026\"}"))
                .andExpect(status().isNoContent());
        var changed = mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"default-admin-test\",\"password\":\"new-admin-test-pass-2026\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.passwordChangeRequired").value(false)).andReturn();
        mockMvc.perform(get("/api/users").session((MockHttpSession) changed.getRequest().getSession(false))).andExpect(status().isOk());
    }

    @Autowired private com.example.BuiThiDiemTrinh_TH5.repository.EmployeeRepository employees;
    @Autowired private com.example.BuiThiDiemTrinh_TH5.repository.WorkShiftRepository shifts;

    @Test
    void cashierCannotOpenCloseOrReportAnotherEmployeesShift() throws Exception {
        var own = new com.example.BuiThiDiemTrinh_TH5.entity.Employee(); own.setName("Own"); own.setPosition("CASHIER"); own.setSalary(java.math.BigDecimal.ZERO); own = employees.save(own);
        var other = new com.example.BuiThiDiemTrinh_TH5.entity.Employee(); other.setName("Other"); other.setPosition("CASHIER"); other.setSalary(java.math.BigDecimal.ZERO); other = employees.save(other);
        User account = userRepository.findByUsernameIgnoreCase("security-cashier").orElseThrow(); account.setEmployee(own); userRepository.save(account);
        var shift = new com.example.BuiThiDiemTrinh_TH5.entity.WorkShift(); shift.setEmployee(other); shift.setOpeningCash(java.math.BigDecimal.ZERO); shift = shifts.save(shift);
        var principal = org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("security-cashier").roles("CASHIER");
        mockMvc.perform(get("/api/shifts").with(principal)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(post("/api/shifts/open").with(principal).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\":" + other.getId() + ",\"openingCash\":0}")) .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/shifts/" + shift.getId() + "/close").with(principal).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"closingCash\":0}")) .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/shifts/" + shift.getId() + "/daily-report").with(principal)).andExpect(status().isForbidden());
    }
    @Test
    void posConnectionRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/pos-connection")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanReadPosConnections() throws Exception {
        mockMvc.perform(get("/api/pos-connection")).andExpect(status().isOk());
    }

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
    void guestQrMenuIsPublicButStaffApprovalIsProtected() throws Exception {
        mockMvc.perform(get("/api/guest/qr/not-a-token"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/qr/orders/1/approve").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":1}"))
                .andExpect(status().isUnauthorized());
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
