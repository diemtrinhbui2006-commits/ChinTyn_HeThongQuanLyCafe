package com.example.BuiThiDiemTrinh_TH5.controller;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import com.example.BuiThiDiemTrinh_TH5.dto.OrderFoodRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.OrderItemRequest;
import com.example.BuiThiDiemTrinh_TH5.entity.*;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.exception.ResourceNotFoundException;
import com.example.BuiThiDiemTrinh_TH5.repository.*;
import com.example.BuiThiDiemTrinh_TH5.service.InvoiceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class OperationsController {
    private final IngredientRepository ingredients;
    private final StockMovementRepository movements;
    private final TableReservationRepository reservations;
    private final CafeTableRepository tables;
    private final WorkShiftRepository shifts;
    private final EmployeeRepository employees;
    private final InvoiceRepository invoices;
    private final ExpenseRepository expenses;
    private final SupplierRepository suppliers;
    private final PromotionRepository promotions;
    private final CustomerRepository customers;
    private final LoyaltyEntryRepository loyalty;
    private final LoyaltyRewardRepository rewards;
    private final ProductRepository products;
    private final QrGuestOrderRepository guestOrders;
    private final InvoiceService invoiceService;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public OperationsController(IngredientRepository ingredients, StockMovementRepository movements,
            TableReservationRepository reservations, CafeTableRepository tables, WorkShiftRepository shifts,
            EmployeeRepository employees, InvoiceRepository invoices, ExpenseRepository expenses,
            SupplierRepository suppliers, PromotionRepository promotions, CustomerRepository customers,
            LoyaltyEntryRepository loyalty, LoyaltyRewardRepository rewards, ProductRepository products, QrGuestOrderRepository guestOrders,
            InvoiceService invoiceService, ObjectMapper objectMapper) {
        this.ingredients = ingredients; this.movements = movements; this.reservations = reservations;
        this.tables = tables; this.shifts = shifts; this.employees = employees; this.invoices = invoices;
        this.expenses = expenses; this.suppliers = suppliers; this.promotions = promotions;
        this.customers = customers; this.loyalty = loyalty; this.rewards = rewards; this.products = products;
        this.guestOrders = guestOrders; this.invoiceService = invoiceService; this.objectMapper = objectMapper;
    }

    @GetMapping("/api/inventory/ingredients")
    public List<Ingredient> ingredients(@RequestParam(defaultValue = "false") boolean lowStock) {
        List<Ingredient> all = ingredients.findAll();
        return lowStock ? all.stream().filter(i -> i.getQuantity().compareTo(i.getLowStockThreshold()) <= 0).toList() : all;
    }

    @GetMapping("/api/inventory/products/low-stock")
    public List<ProductStockView> lowStockProducts(@RequestParam(defaultValue = "5") int threshold) {
        if (threshold < 0) throw new BadRequestException("Ngưỡng cảnh báo không được âm");
        return products.findAll().stream().filter(p -> p.getQuantity() != null && p.getQuantity() <= threshold)
                .map(p -> new ProductStockView(p.getId(), p.getName(), p.getQuantity(), threshold)).toList();
    }

    @PostMapping("/api/inventory/ingredients")
    @ResponseStatus(HttpStatus.CREATED)
    public Ingredient createIngredient(@RequestBody IngredientInput in) {
        validateIngredient(in);
        Ingredient ingredient = new Ingredient();
        ingredient.setName(in.name().trim()); ingredient.setUnit(in.unit().trim());
        ingredient.setQuantity(BigDecimal.ZERO); ingredient.setLowStockThreshold(in.lowStockThreshold());
        return ingredients.save(ingredient);
    }

    @PutMapping("/api/inventory/ingredients/{id}")
    public Ingredient updateIngredient(@PathVariable Long id, @RequestBody IngredientInput in) {
        validateIngredient(in);
        Ingredient ingredient = ingredient(id);
        ingredient.setName(in.name().trim()); ingredient.setUnit(in.unit().trim());
        ingredient.setLowStockThreshold(in.lowStockThreshold());
        return ingredients.save(ingredient);
    }

    @PostMapping("/api/inventory/ingredients/{id}/adjust")
    @Transactional
    public Ingredient adjustIngredient(@PathVariable Long id, @RequestBody StockInput in) {
        if (in.quantity() == null || in.quantity().signum() == 0) throw new BadRequestException("Số lượng điều chỉnh không được bằng 0");
        Ingredient ingredient = ingredient(id);
        BigDecimal updated = ingredient.getQuantity().add(in.quantity());
        if (updated.signum() < 0) throw new BadRequestException("Tồn kho nguyên liệu không thể âm");
        ingredient.setQuantity(updated);
        recordMovement(ingredient, in.quantity(), "ADJUST", in.note());
        return ingredients.save(ingredient);
    }

    @GetMapping("/api/inventory/ingredients/{id}/movements")
    public List<StockMovement> movements(@PathVariable Long id) {
        ingredient(id);
        return movements.findByIngredientIdOrderByCreatedAtDesc(id);
    }

    @GetMapping("/api/reservations")
    public List<ReservationView> reservations() {
        return reservations.findAllByOrderByReservedForAsc().stream().map(ReservationView::from).toList();
    }

    @PostMapping("/api/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ReservationView reserve(@RequestBody ReservationInput in) {
        if (in.tableId() == null || in.customerName() == null || in.customerName().isBlank()
                || in.reservedFor() == null || in.reservedFor().isBefore(LocalDateTime.now())
                || in.partySize() == null || in.partySize() < 1) throw new BadRequestException("Thông tin đặt bàn không hợp lệ");
        CafeTable table = table(in.tableId());
        if (table.getStatus() == TableStatus.OUT_OF_SERVICE || table.getStatus() == TableStatus.OCCUPIED
                || reservations.existsByCafeTableIdAndStatus(table.getId(), "BOOKED")) {
            throw new BadRequestException("Bàn hiện không thể đặt");
        }
        TableReservation reservation = new TableReservation();
        reservation.setCafeTable(table); reservation.setCustomerName(in.customerName().trim());
        reservation.setPhone(in.phone()); reservation.setReservedFor(in.reservedFor());
        reservation.setPartySize(in.partySize()); reservation.setNote(in.note());
        table.setStatus(TableStatus.RESERVED); tables.save(table);
        return ReservationView.from(reservations.save(reservation));
    }

    @PostMapping("/api/reservations/{id}/cancel")
    @Transactional
    public ReservationView cancelReservation(@PathVariable Long id) {
        TableReservation reservation = reservations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt bàn #" + id));
        if (!"BOOKED".equals(reservation.getStatus())) throw new BadRequestException("Đặt bàn đã được xử lý");
        reservation.setStatus("CANCELLED");
        CafeTable table = reservation.getCafeTable();
        if (table.getStatus() == TableStatus.RESERVED) table.setStatus(TableStatus.AVAILABLE);
        return ReservationView.from(reservations.save(reservation));
    }

    @PostMapping("/api/shifts/open")
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftView openShift(@RequestBody ShiftOpenInput in) {
        if (in.employeeId() == null || in.openingCash() == null || in.openingCash().signum() < 0)
            throw new BadRequestException("Nhân viên và tiền đầu ca hợp lệ là bắt buộc");
        if (shifts.findFirstByEmployeeIdAndClosedAtIsNullOrderByOpenedAtDesc(in.employeeId()).isPresent())
            throw new BadRequestException("Nhân viên đang có ca chưa đóng");
        WorkShift shift = new WorkShift();
        shift.setEmployee(employee(in.employeeId())); shift.setOpeningCash(in.openingCash());
        return ShiftView.from(shifts.save(shift));
    }

    @PostMapping("/api/shifts/{id}/close")
    @Transactional
    public ShiftView closeShift(@PathVariable Long id, @RequestBody ShiftCloseInput in) {
        WorkShift shift = shifts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca #" + id));
        if (shift.getClosedAt() != null) throw new BadRequestException("Ca làm việc đã đóng");
        if (in.closingCash() == null || in.closingCash().signum() < 0) throw new BadRequestException("Tiền kiểm kê không hợp lệ");
        LocalDateTime end = LocalDateTime.now();
        BigDecimal cashSales = invoices.totalPaidByMethodBetween(PaymentMethod.CASH, shift.getOpenedAt(), end);
        BigDecimal cashExpenses = expenses.totalByPaymentBetween(PaymentMethod.CASH, shift.getOpenedAt(), end);
        BigDecimal expected = shift.getOpeningCash().add(cashSales).subtract(cashExpenses);
        shift.setClosedAt(end); shift.setClosingCash(in.closingCash()); shift.setExpectedCash(expected); shift.setNote(in.note());
        return ShiftView.from(shifts.save(shift));
    }

    @GetMapping("/api/shifts")
    public List<ShiftView> shifts() { return shifts.findAllByOrderByOpenedAtDesc().stream().map(ShiftView::from).toList(); }

    @GetMapping("/api/suppliers")
    public List<Supplier> suppliers() { return suppliers.findAll(); }
    @PostMapping("/api/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    public Supplier createSupplier(@RequestBody Supplier in) {
        if (in.getName() == null || in.getName().isBlank()) throw new BadRequestException("Tên nhà cung cấp là bắt buộc");
        in.setId(null); return suppliers.save(in);
    }
    @PutMapping("/api/suppliers/{id}")
    public Supplier updateSupplier(@PathVariable Long id, @RequestBody Supplier in) {
        Supplier current = suppliers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp"));
        if (in.getName() == null || in.getName().isBlank()) throw new BadRequestException("Tên nhà cung cấp là bắt buộc");
        current.setName(in.getName().trim()); current.setContactName(in.getContactName()); current.setPhone(in.getPhone());
        current.setEmail(in.getEmail()); current.setAddress(in.getAddress()); return suppliers.save(current);
    }

    @GetMapping("/api/expenses")
    public List<ExpenseView> expenses() { return expenses.findAll().stream().map(ExpenseView::from).toList(); }

    @PostMapping("/api/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ExpenseView addExpense(@RequestBody ExpenseInput in) {
        if (in.description() == null || in.description().isBlank() || in.category() == null || in.category().isBlank()
                || in.amount() == null || in.amount().signum() <= 0 || in.paymentMethod() == null)
            throw new BadRequestException("Thông tin chi phí không hợp lệ");
        Expense expense = new Expense();
        expense.setDescription(in.description().trim()); expense.setCategory(in.category().trim());
        expense.setAmount(in.amount()); expense.setPaymentMethod(in.paymentMethod());
        if (in.supplierId() != null) expense.setSupplier(suppliers.findById(in.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp")));
        if (in.ingredientId() != null) {
            if (in.stockQuantity() == null || in.stockQuantity().signum() <= 0) throw new BadRequestException("Số lượng nhập kho phải lớn hơn 0");
            Ingredient ingredient = ingredient(in.ingredientId());
            ingredient.setQuantity(ingredient.getQuantity().add(in.stockQuantity()));
            ingredients.save(ingredient); recordMovement(ingredient, in.stockQuantity(), "PURCHASE", in.description());
            expense.setIngredient(ingredient); expense.setStockQuantity(in.stockQuantity());
        }
        return ExpenseView.from(expenses.save(expense));
    }

    @GetMapping("/api/promotions")
    public List<Promotion> promotions() { return promotions.findAll(); }
    @PostMapping("/api/promotions")
    @ResponseStatus(HttpStatus.CREATED)
    public Promotion createPromotion(@RequestBody Promotion in) {
        validatePromotion(in); in.setId(null); in.setCode(in.getCode().trim().toUpperCase(Locale.ROOT)); in.setUsageCount(0);
        return promotions.save(in);
    }
    @PutMapping("/api/promotions/{id}")
    public Promotion updatePromotion(@PathVariable Long id, @RequestBody Promotion in) {
        Promotion current = promotions.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi"));
        validatePromotion(in); current.setCode(in.getCode().trim().toUpperCase(Locale.ROOT)); current.setName(in.getName());
        current.setDiscountType(in.getDiscountType().toUpperCase(Locale.ROOT)); current.setDiscountValue(in.getDiscountValue());
        current.setMinimumSpend(in.getMinimumSpend()); current.setStartsAt(in.getStartsAt()); current.setEndsAt(in.getEndsAt());
        current.setActive(in.isActive()); current.setUsageLimit(in.getUsageLimit()); return promotions.save(current);
    }

    @GetMapping("/api/customers/{id}/loyalty")
    public LoyaltyView loyaltyHistory(@PathVariable Long id) {
        Customer customer = customers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng"));
        return new LoyaltyView(customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints(),
                loyalty.findByCustomerIdOrderByCreatedAtDesc(id).stream().map(LoyaltyRecord::from).toList());
    }
    @PostMapping("/api/customers/{id}/loyalty/redeem")
    @Transactional
    public LoyaltyRecord redeem(@PathVariable Long id, @RequestBody RedeemInput in) {
        if (in.points() == null || in.points() <= 0 || in.points() % 100 != 0)
            throw new BadRequestException("Đổi điểm theo bội số 100");
        Customer customer = customers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng"));
        int current = customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints();
        if (current < in.points()) throw new BadRequestException("Khách hàng không đủ điểm");
        customer.setLoyaltyPoints(current - in.points()); customers.save(customer);
        LoyaltyEntry entry = new LoyaltyEntry(); entry.setCustomer(customer); entry.setPoints(-in.points());
        entry.setEntryType("REDEEM"); entry.setDescription("Đổi điểm lấy phần thưởng"); return LoyaltyRecord.from(loyalty.save(entry));
    }

    @GetMapping("/api/rewards")
    public List<LoyaltyReward> rewards() { return rewards.findByActiveTrueOrderByPointsCostAsc(); }
    @PostMapping("/api/rewards")
    @ResponseStatus(HttpStatus.CREATED)
    public LoyaltyReward createReward(@RequestBody RewardInput in) {
        validateReward(in);
        LoyaltyReward reward = new LoyaltyReward(); reward.setName(in.name().trim());
        reward.setDescription(in.description()); reward.setPointsCost(in.pointsCost()); reward.setActive(true);
        return rewards.save(reward);
    }
    @PutMapping("/api/rewards/{id}")
    public LoyaltyReward updateReward(@PathVariable Long id, @RequestBody RewardInput in) {
        validateReward(in);
        LoyaltyReward reward = rewards.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phần thưởng"));
        reward.setName(in.name().trim()); reward.setDescription(in.description());
        reward.setPointsCost(in.pointsCost()); reward.setActive(in.active()); return rewards.save(reward);
    }
    @PostMapping("/api/customers/{id}/loyalty/redeem-reward")
    @Transactional
    public LoyaltyRecord redeemReward(@PathVariable Long id, @RequestBody RewardRedeemInput in) {
        if (in.rewardId() == null) throw new BadRequestException("Vui lòng chọn phần thưởng");
        Customer customer = customers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng"));
        LoyaltyReward reward = rewards.findById(in.rewardId()).filter(LoyaltyReward::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Phần thưởng không khả dụng"));
        int current = customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints();
        if (current < reward.getPointsCost()) throw new BadRequestException("Khách hàng không đủ điểm đổi phần thưởng");
        customer.setLoyaltyPoints(current - reward.getPointsCost()); customers.save(customer);
        LoyaltyEntry entry = new LoyaltyEntry(); entry.setCustomer(customer); entry.setPoints(-reward.getPointsCost());
        entry.setEntryType("REDEEM"); entry.setDescription("Đổi phần thưởng: " + reward.getName());
        return LoyaltyRecord.from(loyalty.save(entry));
    }

    @PostMapping("/api/qr/tables/{tableId}/token")
    @Transactional
    public Map<String, String> createQrToken(@PathVariable Long tableId) {
        CafeTable table = table(tableId);
        if (table.getStatus() == TableStatus.OUT_OF_SERVICE || table.getStatus() == TableStatus.RESERVED)
            throw new BadRequestException("Bàn hiện không nhận gọi món qua QR");
        byte[] bytes = new byte[32]; secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        guestOrders.findByCafeTableIdAndStatus(tableId, "PENDING").forEach(old -> old.setStatus("REVOKED"));
        QrGuestOrder qr = new QrGuestOrder(); qr.setToken(token); qr.setCafeTable(table); qr.setItemsJson("[]");
        guestOrders.save(qr);
        return Map.of("token", token, "url", "/guest-order.html?token=" + token);
    }

    @GetMapping(value = "/api/qr/tokens/{token}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrImage(@PathVariable String token, HttpServletRequest request) {
        latestToken(token);
        String guestUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath() + "/guest-order.html")
                .replaceQuery(null)
                .queryParam("token", token)
                .build().toUriString();
        try {
            var matrix = new QRCodeWriter().encode(guestUrl, BarcodeFormat.QR_CODE, 320, 320);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | java.io.IOException exception) {
            throw new BadRequestException("Không thể tạo mã QR");
        }
    }

    @GetMapping("/api/guest/qr/{token}")
    public GuestMenu guestMenu(@PathVariable String token) {
        QrGuestOrder qr = latestToken(token);
        CafeTable table = qr.getCafeTable();
        return new GuestMenu(table.getTableNumber(), products.findAll().stream().filter(Product::isAvailable)
                .map(p -> new MenuItem(p.getId(), p.getName(), p.getDescription(), p.getPrice())).toList());
    }

    @PostMapping("/api/guest/qr/{token}/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public GuestOrderView guestOrder(@PathVariable String token, @RequestBody GuestOrderInput in) {
        QrGuestOrder qr = latestToken(token);
        if (!"PENDING".equals(qr.getStatus())) throw new BadRequestException("Mã QR này đã được sử dụng hoặc vô hiệu");
        if (in.items() == null || in.items().isEmpty()) throw new BadRequestException("Vui lòng chọn ít nhất một món");
        for (OrderItemRequest item : in.items()) {
            if (item.productId() == null || item.quantity() == null || item.quantity() < 1
                    || products.findById(item.productId()).filter(Product::isAvailable).isEmpty())
                throw new BadRequestException("Danh sách món không hợp lệ");
        }
        try { qr.setItemsJson(objectMapper.writeValueAsString(in.items())); }
        catch (Exception ex) { throw new BadRequestException("Không thể lưu yêu cầu món"); }
        qr.setGuestName(in.guestName()); qr.setGuestPhone(in.guestPhone());
        qr.setStatus("QUEUED");
        return GuestOrderView.from(guestOrders.save(qr));
    }

    @GetMapping("/api/qr/orders/pending")
    public List<GuestOrderView> pendingGuestOrders() {
        return guestOrders.findByStatusOrderByCreatedAtAsc("QUEUED").stream().map(GuestOrderView::from).toList();
    }

    @PostMapping("/api/qr/orders/{id}/approve")
    @Transactional
    public GuestOrderView approveGuestOrder(@PathVariable Long id, @RequestBody ApprovalInput in) {
        QrGuestOrder qr = guestOrders.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu QR"));
        if (!"QUEUED".equals(qr.getStatus())) throw new BadRequestException("Yêu cầu QR đã được xử lý");
        List<OrderItemRequest> items;
        try { items = objectMapper.readValue(qr.getItemsJson(), new TypeReference<>() {}); }
        catch (Exception ex) { throw new BadRequestException("Dữ liệu yêu cầu QR bị lỗi"); }
        Invoice invoice = invoiceService.orderFood(new OrderFoodRequest(qr.getCafeTable().getId(), in.employeeId(), null, null, null, items));
        qr.setApprovedInvoice(invoice); qr.setStatus("APPROVED");
        return GuestOrderView.from(guestOrders.save(qr));
    }

    private QrGuestOrder latestToken(String token) {
        return guestOrders.findByToken(token).filter(q -> "PENDING".equals(q.getStatus()) || "QUEUED".equals(q.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Mã QR không hợp lệ hoặc đã hết hạn"));
    }
    private Ingredient ingredient(Long id) {
        return ingredients.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nguyên liệu #" + id));
    }
    private CafeTable table(Long id) { return tables.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn #" + id)); }
    private Employee employee(Long id) { return employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên #" + id)); }
    private void recordMovement(Ingredient ingredient, BigDecimal quantity, String type, String note) {
        StockMovement movement = new StockMovement(); movement.setIngredient(ingredient);
        movement.setQuantity(quantity); movement.setMovementType(type); movement.setNote(note); movements.save(movement);
    }
    private void validateIngredient(IngredientInput in) {
        if (in.name() == null || in.name().isBlank() || in.unit() == null || in.unit().isBlank()
                || in.lowStockThreshold() == null || in.lowStockThreshold().signum() < 0)
            throw new BadRequestException("Tên, đơn vị và ngưỡng tồn kho hợp lệ là bắt buộc");
    }
    private void validatePromotion(Promotion in) {
        if (in.getCode() == null || in.getCode().isBlank() || in.getName() == null || in.getName().isBlank()
                || in.getDiscountValue() == null || in.getDiscountValue().signum() < 0
                || !List.of("PERCENT", "AMOUNT").contains(in.getDiscountType() == null ? "" : in.getDiscountType().toUpperCase(Locale.ROOT))
                || ("PERCENT".equalsIgnoreCase(in.getDiscountType()) && in.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0)
                || (in.getMinimumSpend() != null && in.getMinimumSpend().signum() < 0)
                || (in.getStartsAt() != null && in.getEndsAt() != null && in.getEndsAt().isBefore(in.getStartsAt()))
                || (in.getUsageLimit() != null && in.getUsageLimit() < 1))
            throw new BadRequestException("Thông tin mã khuyến mãi không hợp lệ");
    }
    private void validateReward(RewardInput in) {
        if (in.name() == null || in.name().isBlank() || in.pointsCost() == null || in.pointsCost() < 1)
            throw new BadRequestException("Tên phần thưởng và điểm đổi phải hợp lệ");
    }

    public record IngredientInput(String name, String unit, BigDecimal lowStockThreshold) {}
    public record ProductStockView(Long id, String name, Integer quantity, Integer lowStockThreshold) {}
    public record StockInput(BigDecimal quantity, String note) {}
    public record ReservationInput(Long tableId, String customerName, String phone, LocalDateTime reservedFor, Integer partySize, String note) {}
    public record ReservationView(Long id, Long tableId, Integer tableNumber, String customerName, String phone, LocalDateTime reservedFor, Integer partySize, String status, String note) {
        static ReservationView from(TableReservation r) { return new ReservationView(r.getId(), r.getCafeTable().getId(), r.getCafeTable().getTableNumber(), r.getCustomerName(), r.getPhone(), r.getReservedFor(), r.getPartySize(), r.getStatus(), r.getNote()); }
    }
    public record ShiftOpenInput(Long employeeId, BigDecimal openingCash) {}
    public record ShiftCloseInput(BigDecimal closingCash, String note) {}
    public record ShiftView(Long id, Long employeeId, String employeeName, LocalDateTime openedAt, LocalDateTime closedAt, BigDecimal openingCash, BigDecimal expectedCash, BigDecimal closingCash, String note) {
        static ShiftView from(WorkShift s) { return new ShiftView(s.getId(), s.getEmployee().getId(), s.getEmployee().getName(), s.getOpenedAt(), s.getClosedAt(), s.getOpeningCash(), s.getExpectedCash(), s.getClosingCash(), s.getNote()); }
    }
    public record ExpenseInput(String description, String category, BigDecimal amount, PaymentMethod paymentMethod, Long supplierId, Long ingredientId, BigDecimal stockQuantity) {}
    public record ExpenseView(Long id, String description, String category, BigDecimal amount, PaymentMethod paymentMethod, Long supplierId, String supplierName, Long ingredientId, BigDecimal stockQuantity, LocalDateTime createdAt) {
        static ExpenseView from(Expense e) { return new ExpenseView(e.getId(), e.getDescription(), e.getCategory(), e.getAmount(), e.getPaymentMethod(), e.getSupplier() == null ? null : e.getSupplier().getId(), e.getSupplier() == null ? null : e.getSupplier().getName(), e.getIngredient() == null ? null : e.getIngredient().getId(), e.getStockQuantity(), e.getCreatedAt()); }
    }
    public record RedeemInput(Integer points) {}
    public record RewardInput(String name, String description, Integer pointsCost, boolean active) {}
    public record RewardRedeemInput(Long rewardId) {}
    public record LoyaltyView(Integer balance, List<LoyaltyRecord> history) {}
    public record LoyaltyRecord(Long id, Integer points, String entryType, String description, LocalDateTime createdAt, Long invoiceId) {
        static LoyaltyRecord from(LoyaltyEntry e) { return new LoyaltyRecord(e.getId(), e.getPoints(), e.getEntryType(), e.getDescription(), e.getCreatedAt(), e.getInvoice() == null ? null : e.getInvoice().getId()); }
    }
    public record MenuItem(Long id, String name, String description, BigDecimal price) {}
    public record GuestMenu(Integer tableNumber, List<MenuItem> items) {}
    public record GuestOrderInput(String guestName, String guestPhone, List<OrderItemRequest> items) {}
    public record GuestOrderView(Long id, String status, Integer tableNumber, String guestName, String guestPhone, String itemsJson, LocalDateTime createdAt, Long invoiceId) {
        static GuestOrderView from(QrGuestOrder o) { return new GuestOrderView(o.getId(), o.getStatus(), o.getCafeTable().getTableNumber(), o.getGuestName(), o.getGuestPhone(), o.getItemsJson(), o.getCreatedAt(), o.getApprovedInvoice() == null ? null : o.getApprovedInvoice().getId()); }
    }
    public record ApprovalInput(Long employeeId) {}
}
