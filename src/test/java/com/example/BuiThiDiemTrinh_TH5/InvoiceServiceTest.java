package com.example.BuiThiDiemTrinh_TH5;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.dto.ApplyDiscountRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.CheckoutRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.OrderFoodRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.OrderItemRequest;
import com.example.BuiThiDiemTrinh_TH5.entity.CafeTable;
import com.example.BuiThiDiemTrinh_TH5.entity.Category;
import com.example.BuiThiDiemTrinh_TH5.entity.Employee;
import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceStatus;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;
import com.example.BuiThiDiemTrinh_TH5.entity.Product;
import com.example.BuiThiDiemTrinh_TH5.entity.TableStatus;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.repository.CafeTableRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.CategoryRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.EmployeeRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.InvoiceRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.ProductRepository;
import com.example.BuiThiDiemTrinh_TH5.service.InvoiceService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoiceServiceTest {

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

    @Autowired
    private InvoiceRepository invoiceRepository;

    private CafeTable table;
    private Employee employee;
    private Product coffee;
    private Product tea;

    @BeforeEach
    void setUp() {
        Category cat = new Category();
        cat.setName("Do Uong Test " + System.currentTimeMillis());
        cat.setDescription("Danh muc test");
        cat = categoryRepository.save(cat);

        coffee = new Product();
        coffee.setName("Ca Phe Sua Test");
        coffee.setDescription("Ca phe ngon");
        coffee.setPrice(30000.0);
        coffee.setQuantity(50);
        coffee.setCategory(cat);
        coffee = productRepository.save(coffee);

        tea = new Product();
        tea.setName("Tra Dao Test");
        tea.setDescription("Tra dao thom");
        tea.setPrice(40000.0);
        tea.setQuantity(30);
        tea.setCategory(cat);
        tea = productRepository.save(tea);

        employee = new Employee();
        employee.setName("Nguyen Thi Mong Tuyen");
        employee.setEmail("tuyen." + System.currentTimeMillis() + "@cafe.test");
        employee.setPhone("0912345678");
        employee.setPosition("STAFF");
        employee.setSalary(8000000.0);
        employee = employeeRepository.save(employee);

        table = new CafeTable();
        table.setTableNumber((int) (System.currentTimeMillis() % 9000 + 100));
        table.setCapacity(4);
        table.setStatus(TableStatus.AVAILABLE);
        table = cafeTableRepository.save(table);
    }

    @Test
    @DisplayName("Nghiệp vụ Gọi món: Khi bàn Trống, thêm món chuyển trạng thái bàn sang Đang dùng và tạo hóa đơn OPEN")
    void testOrderFood_OnAvailableTable_SetsTableOccupiedAndCreatesInvoice() {
        OrderFoodRequest request = new OrderFoodRequest(
                table.getId(),
                employee.getId(),
                null,
                coffee.getId(),
                2,
                null
        );

        Invoice invoice = invoiceService.orderFood(request);

        assertNotNull(invoice.getId());
        assertEquals(InvoiceStatus.OPEN, invoice.getStatus());
        assertEquals(new BigDecimal("60000.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("0.00"), invoice.getDiscount());
        assertEquals(new BigDecimal("60000.00"), invoice.getTotalAmount());

        // Kiểm tra trạng thái bàn đã chuyển từ AVAILABLE sang OCCUPIED
        CafeTable updatedTable = cafeTableRepository.findById(table.getId()).orElseThrow();
        assertEquals(TableStatus.OCCUPIED, updatedTable.getStatus());

        // Kiểm tra tồn kho của món đã bị trừ
        Product updatedCoffee = productRepository.findById(coffee.getId()).orElseThrow();
        assertEquals(48, updatedCoffee.getQuantity());
    }

    @Test
    @DisplayName("Nghiệp vụ Gọi món: Gọi thêm món vào bàn Đang dùng sẽ tích lũy số lượng và tính lại tiền")
    void testOrderFood_OnOccupiedTable_AccumulatesQuantityAndRecalculates() {
        // Lần 1: Gọi 2 ly cà phê
        OrderFoodRequest firstOrder = new OrderFoodRequest(
                table.getId(),
                employee.getId(),
                null,
                coffee.getId(),
                2,
                null
        );
        invoiceService.orderFood(firstOrder);

        // Lần 2: Gọi thêm 1 ly cà phê và 1 ly trà đào
        OrderFoodRequest secondOrder = new OrderFoodRequest(
                table.getId(),
                null, // Không cần truyền lại employee khi bàn đã mở
                null,
                null,
                null,
                List.of(
                        new OrderItemRequest(coffee.getId(), 1),
                        new OrderItemRequest(tea.getId(), 1)
                )
        );
        Invoice invoice = invoiceService.orderFood(secondOrder);

        // 3 ly cà phê (3 * 30.000 = 90.000) + 1 ly trà đào (40.000) = 130.000
        assertEquals(2, invoice.getDetails().size());
        assertEquals(new BigDecimal("130000.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("130000.00"), invoice.getTotalAmount());

        // Kiểm tra tồn kho
        Product updatedCoffee = productRepository.findById(coffee.getId()).orElseThrow();
        assertEquals(47, updatedCoffee.getQuantity());
        Product updatedTea = productRepository.findById(tea.getId()).orElseThrow();
        assertEquals(29, updatedTea.getQuantity());
    }

    @Test
    @DisplayName("Nghiệp vụ Chiết khấu: Áp dụng mã giảm giá voucher (GIAM10, GIAM20, VIP, CAFE50K)")
    void testApplyDiscount_VoucherCodes() {
        // Gọi 2 ly trà đào: 2 * 40.000 = 80.000
        OrderFoodRequest order = new OrderFoodRequest(
                table.getId(),
                employee.getId(),
                null,
                tea.getId(),
                2,
                null
        );
        Invoice invoice = invoiceService.orderFood(order);

        // Áp dụng mã GIAM10 (10% của 80.000 = 8.000)
        ApplyDiscountRequest discountReq = new ApplyDiscountRequest("GIAM10", null, null);
        Invoice discountedInvoice = invoiceService.applyDiscount(invoice.getId(), discountReq);

        assertEquals(new BigDecimal("8000.00"), discountedInvoice.getDiscount());
        assertEquals(new BigDecimal("72000.00"), discountedInvoice.getTotalAmount());

        // Áp dụng mã CAFE50K (giảm 50.000)
        ApplyDiscountRequest voucher50k = new ApplyDiscountRequest("CAFE50K", null, null);
        discountedInvoice = invoiceService.applyDiscount(invoice.getId(), voucher50k);

        assertEquals(new BigDecimal("50000.00"), discountedInvoice.getDiscount());
        assertEquals(new BigDecimal("30000.00"), discountedInvoice.getTotalAmount());
    }

    @Test
    @DisplayName("Nghiệp vụ Chiết khấu: Áp dụng theo số tiền và theo phần trăm")
    void testApplyDiscount_AmountAndPercent() {
        // Gọi 1 ly trà đào (40.000)
        Invoice invoice = invoiceService.orderFood(new OrderFoodRequest(
                table.getId(), employee.getId(), null, tea.getId(), 1, null
        ));

        // Giảm trực tiếp 15.000đ
        Invoice discounted = invoiceService.applyDiscount(invoice.getId(),
                new ApplyDiscountRequest(null, new BigDecimal("15000"), null));
        assertEquals(new BigDecimal("15000.00"), discounted.getDiscount());
        assertEquals(new BigDecimal("25000.00"), discounted.getTotalAmount());

        // Giảm theo phần trăm 25% (25% của 40.000 = 10.000)
        discounted = invoiceService.applyDiscount(invoice.getId(),
                new ApplyDiscountRequest(null, null, 25.0));
        assertEquals(new BigDecimal("10000.00"), discounted.getDiscount());
        assertEquals(new BigDecimal("30000.00"), discounted.getTotalAmount());
    }

    @Test
    @DisplayName("Nghiệp vụ Tính hóa đơn & Thanh toán: Chuyển hóa đơn sang PAID, bàn quay về Trống (AVAILABLE)")
    void testCheckout_Success() {
        // 1. Gọi món
        Invoice invoice = invoiceService.orderFood(new OrderFoodRequest(
                table.getId(), employee.getId(), null, coffee.getId(), 2, null
        ));
        assertEquals(TableStatus.OCCUPIED, cafeTableRepository.findById(table.getId()).orElseThrow().getStatus());

        // 2. Thanh toán với mã giảm giá VIP (15%) và phương thức chuyển khoản
        CheckoutRequest checkoutReq = new CheckoutRequest(
                PaymentMethod.BANK_TRANSFER,
                "VIP",
                null,
                null
        );
        Invoice paidInvoice = invoiceService.checkout(invoice.getId(), checkoutReq);

        // Tổng tiền 60.000, giảm 15% = 9.000, khách cần trả = 51.000
        assertEquals(InvoiceStatus.PAID, paidInvoice.getStatus());
        assertEquals(PaymentMethod.BANK_TRANSFER, paidInvoice.getPaymentMethod());
        assertNotNull(paidInvoice.getPaidAt());
        assertEquals(new BigDecimal("9000.00"), paidInvoice.getDiscount());
        assertEquals(new BigDecimal("51000.00"), paidInvoice.getTotalAmount());

        // Kiểm tra bàn tự động chuyển về trạng thái Trống (AVAILABLE)
        CafeTable finalTable = cafeTableRepository.findById(table.getId()).orElseThrow();
        assertEquals(TableStatus.AVAILABLE, finalTable.getStatus());
    }

    @Test
    @DisplayName("Nghiệp vụ Hủy món: Hoàn lại kho và tính lại tổng tiền")
    void testRemoveFoodFromInvoice() {
        Invoice invoice = invoiceService.orderFood(new OrderFoodRequest(
                table.getId(), employee.getId(), null, coffee.getId(), 3, null
        ));
        assertEquals(new BigDecimal("90000.00"), invoice.getSubtotal());
        assertEquals(47, productRepository.findById(coffee.getId()).orElseThrow().getQuantity());

        // Hủy bớt 1 ly cà phê
        Invoice updated = invoiceService.removeFoodFromInvoice(invoice.getId(), coffee.getId(), 1);
        assertEquals(new BigDecimal("60000.00"), updated.getSubtotal());
        assertEquals(48, productRepository.findById(coffee.getId()).orElseThrow().getQuantity());
    }

    @Test
    @DisplayName("Nghiệp vụ Hủy hóa đơn: Hoàn lại toàn bộ kho và chuyển bàn về Trống")
    void testCancelInvoice() {
        Invoice invoice = invoiceService.orderFood(new OrderFoodRequest(
                table.getId(), employee.getId(), null, coffee.getId(), 2, null
        ));
        assertEquals(TableStatus.OCCUPIED, cafeTableRepository.findById(table.getId()).orElseThrow().getStatus());

        Invoice cancelled = invoiceService.cancelInvoice(invoice.getId());
        assertEquals(InvoiceStatus.CANCELLED, cancelled.getStatus());
        assertEquals(50, productRepository.findById(coffee.getId()).orElseThrow().getQuantity());
        assertEquals(TableStatus.AVAILABLE, cafeTableRepository.findById(table.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Bắt lỗi khi gọi món vượt quá tồn kho")
    void testOrderFood_ExceedStock_ThrowsBadRequest() {
        OrderFoodRequest request = new OrderFoodRequest(
                table.getId(),
                employee.getId(),
                null,
                coffee.getId(),
                999, // Vượt quá 50 trong kho
                null
        );

        assertThrows(BadRequestException.class, () -> invoiceService.orderFood(request));
    }

    @Test
    @DisplayName("Bắt lỗi khi gọi món vào bàn đang ngưng phục vụ")
    void testOrderFood_OutOfServiceTable_ThrowsBadRequest() {
        table.setStatus(TableStatus.OUT_OF_SERVICE);
        cafeTableRepository.save(table);

        OrderFoodRequest request = new OrderFoodRequest(
                table.getId(),
                employee.getId(),
                null,
                coffee.getId(),
                1,
                null
        );

        assertThrows(BadRequestException.class, () -> invoiceService.orderFood(request));
    }
}
