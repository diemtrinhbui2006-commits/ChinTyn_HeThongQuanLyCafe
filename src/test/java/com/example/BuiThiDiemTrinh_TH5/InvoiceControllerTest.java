package com.example.BuiThiDiemTrinh_TH5;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.dto.ApplyDiscountRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.CheckoutRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.OrderFoodRequest;
import com.example.BuiThiDiemTrinh_TH5.entity.CafeTable;
import com.example.BuiThiDiemTrinh_TH5.entity.Category;
import com.example.BuiThiDiemTrinh_TH5.entity.Employee;
import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;
import com.example.BuiThiDiemTrinh_TH5.entity.Product;
import com.example.BuiThiDiemTrinh_TH5.entity.TableStatus;
import com.example.BuiThiDiemTrinh_TH5.repository.CafeTableRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.CategoryRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.EmployeeRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.ProductRepository;
import com.example.BuiThiDiemTrinh_TH5.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "CASHIER")
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private CafeTableRepository cafeTableRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private CafeTable table;
    private Employee employee;
    private Product product;

    @BeforeEach
    void setUp() {
        Category cat = new Category();
        cat.setName("Do Uong Controller Test " + System.currentTimeMillis());
        cat = categoryRepository.save(cat);

        product = new Product();
        product.setName("Ca Phe Muoi");
        product.setPrice(new BigDecimal("35000.00"));
        product.setQuantity(40);
        product.setCategory(cat);
        product = productRepository.save(product);

        employee = new Employee();
        employee.setName("Nguyen Thi Mong Tuyen");
        employee.setEmail("tuyen.ctrl." + System.currentTimeMillis() + "@cafe.test");
        employee.setPosition("STAFF");
        employee.setSalary(new BigDecimal("7500000.00"));
        employee = employeeRepository.save(employee);

        table = new CafeTable();
        table.setTableNumber((int) (System.currentTimeMillis() % 8000 + 500));
        table.setCapacity(2);
        table.setStatus(TableStatus.AVAILABLE);
        table = cafeTableRepository.save(table);
    }

    @Test
    @DisplayName("API POST /api/invoices/order: Gọi món tạo hóa đơn thành công và chuyển bàn sang OCCUPIED")
    void testOrderFoodEndpoint() throws Exception {
        OrderFoodRequest request = new OrderFoodRequest(
                table.getId(),
                employee.getId(),
                null,
                product.getId(),
                2,
                null
        );

        mockMvc.perform(post("/api/invoices/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.subtotal").value(70000.0))
                .andExpect(jsonPath("$.totalAmount").value(70000.0))
                .andExpect(jsonPath("$.details[0].productName").value("Ca Phe Muoi"))
                .andExpect(jsonPath("$.details[0].quantity").value(2));
    }

    @Test
    @DisplayName("API GET /api/invoices/table/{tableId}: Xem hóa đơn đang mở của bàn")
    void testGetOpenInvoiceByTableEndpoint() throws Exception {
        invoiceService.orderFood(new OrderFoodRequest(
                table.getId(), employee.getId(), null, product.getId(), 1, null
        ));

        mockMvc.perform(get("/api/invoices/table/" + table.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.tableId").value(table.getId()))
                .andExpect(jsonPath("$.subtotal").value(35000.0));
    }

    @Test
    @DisplayName("API POST /api/invoices/{id}/discount: Áp dụng chiết khấu voucher")
    void testApplyDiscountEndpoint() throws Exception {
        Invoice invoice = invoiceService.orderFood(new OrderFoodRequest(
                table.getId(), employee.getId(), null, product.getId(), 2, null
        ));

        ApplyDiscountRequest discountReq = new ApplyDiscountRequest("GIAM10", null, null);

        mockMvc.perform(post("/api/invoices/" + invoice.getId() + "/discount")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount").value(7000.0))
                .andExpect(jsonPath("$.totalAmount").value(63000.0));
    }

    @Test
    @DisplayName("API POST /api/invoices/{id}/checkout: Thanh toán và chuyển bàn về AVAILABLE")
    void testCheckoutEndpoint() throws Exception {
        Invoice invoice = invoiceService.orderFood(new OrderFoodRequest(
                table.getId(), employee.getId(), null, product.getId(), 1, null
        ));

        CheckoutRequest checkoutReq = new CheckoutRequest(
                PaymentMethod.CASH,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/invoices/" + invoice.getId() + "/checkout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.totalAmount").value(35000.0));
    }
}
