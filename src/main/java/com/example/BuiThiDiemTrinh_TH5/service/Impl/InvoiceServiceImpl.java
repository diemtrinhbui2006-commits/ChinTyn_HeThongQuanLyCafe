package com.example.BuiThiDiemTrinh_TH5.service.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.dto.ApplyDiscountRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.CheckoutRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.OrderFoodRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.OrderItemRequest;
import com.example.BuiThiDiemTrinh_TH5.entity.CafeTable;
import com.example.BuiThiDiemTrinh_TH5.entity.Customer;
import com.example.BuiThiDiemTrinh_TH5.entity.Employee;
import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceDetail;
import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceStatus;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;
import com.example.BuiThiDiemTrinh_TH5.entity.Product;
import com.example.BuiThiDiemTrinh_TH5.entity.TableStatus;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.exception.ResourceNotFoundException;
import com.example.BuiThiDiemTrinh_TH5.repository.CafeTableRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.CustomerRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.EmployeeRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.InvoiceRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.ProductRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.PromotionRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.LoyaltyEntryRepository;
import com.example.BuiThiDiemTrinh_TH5.entity.Promotion;
import com.example.BuiThiDiemTrinh_TH5.entity.LoyaltyEntry;
import com.example.BuiThiDiemTrinh_TH5.service.InvoiceService;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CafeTableRepository cafeTableRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final PromotionRepository promotionRepository;
    private final LoyaltyEntryRepository loyaltyEntryRepository;

    @Autowired
    public InvoiceServiceImpl(
            InvoiceRepository invoiceRepository,
            CafeTableRepository cafeTableRepository,
            ProductRepository productRepository,
            EmployeeRepository employeeRepository,
            CustomerRepository customerRepository,
            PromotionRepository promotionRepository,
            LoyaltyEntryRepository loyaltyEntryRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.cafeTableRepository = cafeTableRepository;
        this.productRepository = productRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.promotionRepository = promotionRepository;
        this.loyaltyEntryRepository = loyaltyEntryRepository;
    }

    public InvoiceServiceImpl(
            InvoiceRepository invoiceRepository,
            CafeTableRepository cafeTableRepository,
            ProductRepository productRepository,
            EmployeeRepository employeeRepository,
            CustomerRepository customerRepository
    ) {
        this(invoiceRepository, cafeTableRepository, productRepository, employeeRepository, customerRepository, null, null);
    }

    @Override
    @Transactional
    public Invoice orderFood(OrderFoodRequest request) {
        if (request == null) {
            throw new BadRequestException("Yêu cầu gọi món không được để trống");
        }
        if (request.tableId() == null) {
            throw new BadRequestException("Bàn (tableId) không được để trống");
        }

        CafeTable table = cafeTableRepository.findById(request.tableId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn có id = " + request.tableId()));

        if (table.getStatus() == TableStatus.OUT_OF_SERVICE) {
            throw new BadRequestException("Bàn số " + table.getTableNumber() + " hiện đang ngừng sử dụng, không thể gọi món");
        }
        if (table.getStatus() == TableStatus.RESERVED) {
            throw new BadRequestException("Bàn số " + table.getTableNumber() + " đang được đặt trước, không thể nhận khách");
        }

        // Tập hợp danh sách món gọi
        List<OrderItemRequest> itemsToOrder = new ArrayList<>();
        if (request.items() != null && !request.items().isEmpty()) {
            itemsToOrder.addAll(request.items());
        }
        if (request.productId() != null && request.quantity() != null) {
            itemsToOrder.add(new OrderItemRequest(request.productId(), request.quantity()));
        }
        if (itemsToOrder.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ít nhất một món để thêm vào bàn");
        }

        // Tìm hóa đơn OPEN hiện tại của bàn hoặc tạo mới
        Optional<Invoice> openInvoiceOpt = invoiceRepository.findFirstByCafeTableIdAndStatus(table.getId(), InvoiceStatus.OPEN);
        Invoice invoice;

        if (openInvoiceOpt.isEmpty()) {
            // Mở bàn mới: Kiểm tra nhân viên phục vụ
            if (request.employeeId() == null) {
                throw new BadRequestException("Cần chỉ định nhân viên phục vụ khi mở bàn mới");
            }
            Employee employee = employeeRepository.findById(request.employeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên có id = " + request.employeeId()));

            Customer customer = null;
            if (request.customerId() != null) {
                customer = customerRepository.findById(request.customerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng có id = " + request.customerId()));
            }

            invoice = new Invoice();
            invoice.setCafeTable(table);
            invoice.setEmployee(employee);
            invoice.setCustomer(customer);
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setStatus(InvoiceStatus.OPEN);
            invoice.setSubtotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            invoice.setDiscount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            invoice.setTotalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

            // Chuyển đổi trạng thái bàn từ "Trống" sang "Đang dùng"
            table.setStatus(TableStatus.OCCUPIED);
            cafeTableRepository.save(table);
        } else {
            invoice = openInvoiceOpt.get();

            // Đảm bảo trạng thái bàn là OCCUPIED
            if (table.getStatus() != TableStatus.OCCUPIED) {
                table.setStatus(TableStatus.OCCUPIED);
                cafeTableRepository.save(table);
            }

            // Gán khách hàng nếu chưa có và request có truyền
            if (invoice.getCustomer() == null && request.customerId() != null) {
                customerRepository.findById(request.customerId()).ifPresent(invoice::setCustomer);
            }
        }

        // Xử lý từng món gọi
        for (OrderItemRequest item : itemsToOrder) {
            if (item == null) throw new BadRequestException("Món gọi không hợp lệ");
            int sugar = item.sugarPercent() == null ? 100 : item.sugarPercent();
            String ice = item.iceOption() == null ? "NORMAL" : item.iceOption();
            String note = item.note() == null ? "" : item.note().trim();
            if (!java.util.Set.of(100, 70, 50, 30).contains(sugar)
                    || !java.util.Set.of("NORMAL", "NONE").contains(ice) || note.length() > 500) {
                throw new BadRequestException("Tùy chọn đường, đá hoặc ghi chú không hợp lệ (tối đa 500 ký tự)");
            }
            String optionKey = "DEFAULT";
            if (sugar != 100 || !ice.equals("NORMAL") || !note.isEmpty()) {
                try {
                    optionKey = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                            .digest((sugar + "|" + ice + "|" + note).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                } catch (java.security.NoSuchAlgorithmException exception) {
                    throw new IllegalStateException(exception);
                }
            }
            final String variantKey = optionKey;
            if (item.productId() == null) {
                throw new BadRequestException("Mã món (productId) không được để trống");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new BadRequestException("Số lượng gọi món phải lớn hơn 0");
            }

            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món có id = " + item.productId()));
            if (product.isDeleted() || !product.isAvailable()) {
                throw new BadRequestException("Món đã xóa hoặc ngừng bán, không thể gọi thêm");
            }

            // Kiểm tra tồn kho
            if (product.getQuantity() != null && product.getQuantity() < item.quantity()) {
                throw new BadRequestException("Món '" + product.getName() + "' không đủ số lượng trong kho (còn: " + product.getQuantity() + ")");
            }

            // Trừ tồn kho
            if (product.getQuantity() != null) {
                product.setQuantity(product.getQuantity() - item.quantity());
                productRepository.save(product);
            }

            // Kiểm tra xem món đã có trong chi tiết hóa đơn hay chưa
            Optional<InvoiceDetail> existingDetailOpt = invoice.getDetails().stream()
                    .filter(d -> d.getProduct() != null && d.getProduct().getId().equals(product.getId()))
                    .filter(d -> variantKey.equals(d.getOptionKey()))
                    .findFirst();

            if (existingDetailOpt.isPresent()) {
                InvoiceDetail detail = existingDetailOpt.get();
                int newQty = detail.getQuantity() + item.quantity();
                detail.setQuantity(newQty);
                detail.setLineTotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(newQty)).setScale(2, RoundingMode.HALF_UP));
            } else {
                BigDecimal unitPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity())).setScale(2, RoundingMode.HALF_UP);
                InvoiceDetail detail = new InvoiceDetail(
                        null,
                        invoice,
                        product,
                        item.quantity(),
                        unitPrice,
                        lineTotal
                );
                detail.setSugarPercent(sugar);
                detail.setIceOption(ice);
                detail.setNote(note);
                detail.setOptionKey(variantKey);
                invoice.getDetails().add(detail);
            }
        }

        // Tính lại tổng tiền
        recalculateInvoiceAmounts(invoice);
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getOpenInvoiceByTableId(Long tableId) {
        if (tableId == null) {
            throw new BadRequestException("Mã bàn không được để trống");
        }
        return invoiceRepository.findFirstByCafeTableIdAndStatus(tableId, InvoiceStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn có id = " + tableId + " hiện không có hóa đơn đang mở"));
    }

    @Override
    public Invoice getInvoiceById(Long id) {
        if (id == null) {
            throw new BadRequestException("Mã hóa đơn không được để trống");
        }
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn có id = " + id));
    }

    @Override
    public List<Invoice> getInvoices(String status) {
        if (status == null || status.isBlank()) {
            return invoiceRepository.findAll();
        }
        try {
            InvoiceStatus invoiceStatus = InvoiceStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            return invoiceRepository.findByStatus(invoiceStatus);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Trạng thái hóa đơn không hợp lệ: " + status);
        }
    }

    @Override
    @Transactional
    public Invoice applyDiscount(Long invoiceId, ApplyDiscountRequest request) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new BadRequestException("Chỉ có thể áp dụng giảm giá cho hóa đơn đang mở (OPEN)");
        }
        if (request == null) {
            throw new BadRequestException("Thông tin giảm giá không được để trống");
        }

        applyDiscountInternal(invoice, request.discountCode(), request.discountAmount(), request.discountPercent());
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice checkout(Long invoiceId, CheckoutRequest request) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new BadRequestException("Hóa đơn #" + invoiceId + " không ở trạng thái chờ thanh toán (OPEN)");
        }
        if (invoice.getDetails() == null || invoice.getDetails().isEmpty()) {
            throw new BadRequestException("Không thể thanh toán hóa đơn chưa có món ăn/nước uống nào");
        }

        // Áp dụng giảm giá nếu có gửi kèm trong yêu cầu thanh toán
        if (request != null && (request.discountCode() != null || request.discountAmount() != null || request.discountPercent() != null)) {
            applyDiscountInternal(invoice, request.discountCode(), request.discountAmount(), request.discountPercent());
        } else {
            recalculateInvoiceAmounts(invoice);
        }
        validatePromotionAtCheckout(invoice);

        PaymentMethod paymentMethod = (request != null && request.paymentMethod() != null)
                ? request.paymentMethod()
                : PaymentMethod.CASH;

        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setStatus(InvoiceStatus.PAID);
        if (invoice.getPromotionCode() != null && promotionRepository != null) {
            promotionRepository.findByCodeIgnoreCaseForUpdate(invoice.getPromotionCode()).ifPresent(promotion -> {
                promotion.setUsageCount((promotion.getUsageCount() == null ? 0 : promotion.getUsageCount()) + 1);
                promotionRepository.save(promotion);
            });
        }
        awardLoyaltyPoints(invoice);

        // Chuyển đổi trạng thái bàn từ "Đang dùng" (OCCUPIED) trở về "Trống" (AVAILABLE)
        CafeTable table = invoice.getCafeTable();
        if (table != null) {
            table.setStatus(TableStatus.AVAILABLE);
            cafeTableRepository.save(table);
        }

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice removeFoodFromInvoice(Long invoiceId, Long productId, Integer quantity) {
        return removeFoodFromInvoice(invoiceId, productId, quantity, null);
    }

    @Override
    @Transactional
    public Invoice removeFoodFromInvoice(Long invoiceId, Long productId, Integer quantity, Long detailId) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new BadRequestException("Chỉ có thể chỉnh sửa món trên hóa đơn đang mở (OPEN)");
        }
        if (productId == null) {
            throw new BadRequestException("Mã món không được để trống");
        }

        InvoiceDetail detail = invoice.getDetails().stream()
                .filter(d -> d.getProduct() != null && d.getProduct().getId().equals(productId))
                .filter(d -> detailId == null || detailId.equals(d.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Món có id = " + productId + " không có trong hóa đơn #" + invoiceId));

        Product product = detail.getProduct();
        int qtyToRemove = (quantity == null || quantity <= 0 || quantity >= detail.getQuantity())
                ? detail.getQuantity()
                : quantity;

        // Hoàn lại kho
        if (product != null && product.getQuantity() != null) {
            product.setQuantity(product.getQuantity() + qtyToRemove);
            productRepository.save(product);
        }

        if (qtyToRemove >= detail.getQuantity()) {
            invoice.getDetails().remove(detail);
        } else {
            int remainingQty = detail.getQuantity() - qtyToRemove;
            detail.setQuantity(remainingQty);
            detail.setLineTotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(remainingQty)).setScale(2, RoundingMode.HALF_UP));
        }

        recalculateInvoiceAmounts(invoice);
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice cancelInvoice(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new BadRequestException("Chỉ có thể hủy hóa đơn đang ở trạng thái OPEN");
        }

        // Hoàn lại kho toàn bộ các món trong hóa đơn
        if (invoice.getDetails() != null) {
            for (InvoiceDetail detail : invoice.getDetails()) {
                Product product = detail.getProduct();
                if (product != null && product.getQuantity() != null) {
                    product.setQuantity(product.getQuantity() + detail.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);

        // Chuyển bàn trở lại trạng thái "Trống"
        CafeTable table = invoice.getCafeTable();
        if (table != null) {
            table.setStatus(TableStatus.AVAILABLE);
            cafeTableRepository.save(table);
        }

        return invoiceRepository.save(invoice);
    }

    private void applyDiscountInternal(Invoice invoice, String discountCode, BigDecimal discountAmount, Double discountPercent) {
        boolean noCode = discountCode == null || discountCode.isBlank();
        if (noCode && discountAmount != null && discountAmount.signum() == 0 && discountPercent == null) {
            invoice.setPromotionCode(null);
            invoice.setDiscount(BigDecimal.ZERO.setScale(2));
            invoice.setTotalAmount(invoice.getSubtotal());
            return;
        }
        if (noCode || discountAmount != null || discountPercent != null)
            throw new BadRequestException("Vui lòng nhập mã khuyến mãi đã tạo; không được tự nhập số tiền hoặc phần trăm giảm giá");
        Promotion promotion = validPromotion(invoice, discountCode);
        BigDecimal discount = promotionDiscount(invoice, promotion);
        invoice.setPromotionCode(promotion.getCode());
        invoice.setDiscount(discount);
        invoice.setTotalAmount(invoice.getSubtotal().subtract(discount).setScale(2, RoundingMode.HALF_UP));
    }

    private Promotion validPromotion(Invoice invoice, String code) {
        Promotion promotion = promotionRepository.findByCodeIgnoreCaseForUpdate(code.trim())
                .orElseThrow(() -> new BadRequestException("Mã khuyến mãi không tồn tại: " + code.trim()));
        LocalDateTime now = LocalDateTime.now();
        if (!promotion.isActive() || (promotion.getStartsAt() != null && now.isBefore(promotion.getStartsAt()))
                || (promotion.getEndsAt() != null && !now.isBefore(promotion.getEndsAt()))
                || (promotion.getUsageLimit() != null && (promotion.getUsageCount() == null ? 0 : promotion.getUsageCount()) >= promotion.getUsageLimit()))
            throw new BadRequestException("Mã khuyến mãi đang tắt, chưa đến hạn, hết hạn hoặc đã hết lượt sử dụng");
        if (promotion.getMinimumSpend() != null && invoice.getSubtotal().compareTo(promotion.getMinimumSpend()) < 0)
            throw new BadRequestException("Hóa đơn chưa đạt giá trị tối thiểu của mã khuyến mãi");
        if (promotion.getDiscountValue() == null || promotion.getDiscountValue().signum() < 0
                || !("AMOUNT".equalsIgnoreCase(promotion.getDiscountType()) || "PERCENT".equalsIgnoreCase(promotion.getDiscountType()))
                || ("PERCENT".equalsIgnoreCase(promotion.getDiscountType()) && promotion.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0))
            throw new BadRequestException("Cấu hình mã khuyến mãi không hợp lệ");
        return promotion;
    }

    private BigDecimal promotionDiscount(Invoice invoice, Promotion promotion) {
        BigDecimal value = "AMOUNT".equalsIgnoreCase(promotion.getDiscountType()) ? promotion.getDiscountValue()
                : invoice.getSubtotal().multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return value.min(invoice.getSubtotal()).setScale(2, RoundingMode.HALF_UP);
    }

    private void awardLoyaltyPoints(Invoice invoice) {
        if (invoice.getCustomer() == null || loyaltyEntryRepository == null
                || loyaltyEntryRepository.existsByInvoiceIdAndEntryType(invoice.getId(), "EARN")) return;
        int points = invoice.getTotalAmount().divideToIntegralValue(BigDecimal.valueOf(10000)).intValue();
        if (points <= 0) return;
        Customer customer = invoice.getCustomer();
        customer.setLoyaltyPoints((customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints()) + points);
        customerRepository.save(customer);
        LoyaltyEntry entry = new LoyaltyEntry();
        entry.setCustomer(customer);
        entry.setInvoice(invoice);
        entry.setPoints(points);
        entry.setEntryType("EARN");
        entry.setDescription("Tích điểm từ hóa đơn #" + invoice.getId());
        loyaltyEntryRepository.save(entry);
    }

    private void validatePromotionAtCheckout(Invoice invoice) {
        if (invoice.getPromotionCode() == null) {
            if (invoice.getDiscount().signum() > 0) throw new BadRequestException("Chiết khấu chưa gắn mã hợp lệ. Hãy bỏ giảm giá hoặc áp dụng lại mã đã tạo.");
            return;
        }
        Promotion promotion = validPromotion(invoice, invoice.getPromotionCode());
        if (promotionDiscount(invoice, promotion).compareTo(invoice.getDiscount()) != 0)
            throw new BadRequestException("Tiền hàng hoặc ưu đãi đã thay đổi. Hãy áp dụng lại mã khuyến mãi trước khi thanh toán.");
    }

    private void recalculateInvoiceAmounts(Invoice invoice) {
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (invoice.getDetails() != null) {
            for (InvoiceDetail detail : invoice.getDetails()) {
                if (detail.getLineTotal() != null) {
                    subtotal = subtotal.add(detail.getLineTotal());
                }
            }
        }
        invoice.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));

        BigDecimal discount = invoice.getDiscount() != null ? invoice.getDiscount() : BigDecimal.ZERO;
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        invoice.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));

        BigDecimal totalAmount = subtotal.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        invoice.setTotalAmount(totalAmount);
    }
}
