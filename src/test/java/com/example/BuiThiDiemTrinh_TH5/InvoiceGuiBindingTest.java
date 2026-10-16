package com.example.BuiThiDiemTrinh_TH5;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.dto.InvoiceResponse;
import com.example.BuiThiDiemTrinh_TH5.entity.CafeTable;
import com.example.BuiThiDiemTrinh_TH5.entity.Category;
import com.example.BuiThiDiemTrinh_TH5.entity.Employee;
import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceStatus;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;
import com.example.BuiThiDiemTrinh_TH5.entity.Product;
import com.example.BuiThiDiemTrinh_TH5.entity.TableStatus;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.gui.binding.AddFoodFormData;
import com.example.BuiThiDiemTrinh_TH5.gui.binding.CheckoutFormData;
import com.example.BuiThiDiemTrinh_TH5.gui.binding.InvoiceGuiBinding;
import com.example.BuiThiDiemTrinh_TH5.repository.CafeTableRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.CategoryRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.EmployeeRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.InvoiceRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoiceGuiBindingTest {

    @Autowired
    private InvoiceGuiBinding invoiceGuiBinding;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CafeTableRepository cafeTableRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private CafeTable table;
    private Product product;
    private Employee employee;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Đồ uống kiểm thử GUI");
        category = categoryRepository.save(category);

        product = new Product();
        product.setName("Cà phê giao diện");
        product.setPrice(new BigDecimal("30000.00"));
        product.setQuantity(20);
        product.setCategory(category);
        product = productRepository.save(product);

        employee = new Employee();
        employee.setName("Nguyễn Thị Mộng Tuyền");
        employee.setEmail("tuyen.gui@cafe.test");
        employee.setPosition("STAFF");
        employee.setSalary(new BigDecimal("7500000.00"));
        employee = employeeRepository.save(employee);

        table = new CafeTable();
        table.setTableNumber(99);
        table.setCapacity(4);
        table.setStatus(TableStatus.AVAILABLE);
        table = cafeTableRepository.save(table);
    }

    @Test
    @DisplayName("Nút Thêm món gọi đúng nghiệp vụ và lưu hóa đơn xuống database")
    void addFoodButton_SavesInvoiceAndUpdatesTable() {
        InvoiceResponse response = invoiceGuiBinding.addFood(new AddFoodFormData(
                table.getId().toString(),
                employee.getId().toString(),
                "",
                product.getId().toString(),
                "2"
        ));

        assertNotNull(response.id());
        assertEquals(InvoiceStatus.OPEN, response.status());
        assertEquals(new BigDecimal("60000.00"), response.totalAmount());
        assertEquals(2, response.details().get(0).quantity());

        Invoice savedInvoice = invoiceRepository.findById(response.id()).orElseThrow();
        assertEquals(InvoiceStatus.OPEN, savedInvoice.getStatus());
        assertEquals(TableStatus.OCCUPIED,
                cafeTableRepository.findById(table.getId()).orElseThrow().getStatus());
        assertEquals(18,
                productRepository.findById(product.getId()).orElseThrow().getQuantity());
    }

    @Test
    @DisplayName("Nút Thanh toán gọi đúng nghiệp vụ, lưu PAID và trả bàn về Trống")
    void checkoutButton_SavesPaidInvoiceAndReleasesTable() {
        InvoiceResponse openInvoice = invoiceGuiBinding.addFood(new AddFoodFormData(
                table.getId().toString(),
                employee.getId().toString(),
                null,
                product.getId().toString(),
                "2"
        ));

        InvoiceResponse paidInvoice = invoiceGuiBinding.checkout(new CheckoutFormData(
                openInvoice.id().toString(),
                "Chuyển khoản",
                "GIAM10",
                null,
                null
        ));

        assertEquals(InvoiceStatus.PAID, paidInvoice.status());
        assertEquals(PaymentMethod.BANK_TRANSFER, paidInvoice.paymentMethod());
        assertEquals(new BigDecimal("6000.00"), paidInvoice.discount());
        assertEquals(new BigDecimal("54000.00"), paidInvoice.totalAmount());

        Invoice savedInvoice = invoiceRepository.findById(paidInvoice.id()).orElseThrow();
        assertEquals(InvoiceStatus.PAID, savedInvoice.getStatus());
        assertNotNull(savedInvoice.getPaidAt());
        assertEquals(TableStatus.AVAILABLE,
                cafeTableRepository.findById(table.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Data Binding báo lỗi rõ ràng khi ô số lượng nhập sai")
    void addFoodButton_InvalidQuantity_ShowsValidationError() {
        AddFoodFormData invalidForm = new AddFoodFormData(
                table.getId().toString(),
                employee.getId().toString(),
                null,
                product.getId().toString(),
                "abc"
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> invoiceGuiBinding.addFood(invalidForm)
        );

        assertEquals("Số lượng phải là số nguyên dương", exception.getMessage());
        assertEquals(0, invoiceRepository.count());
    }
}
