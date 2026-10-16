package com.example.BuiThiDiemTrinh_TH5.service.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
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
import com.example.BuiThiDiemTrinh_TH5.service.InvoiceService;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CafeTableRepository cafeTableRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;

    public InvoiceServiceImpl(
            InvoiceRepository invoiceRepository,
            CafeTableRepository cafeTableRepository,
            ProductRepository productRepository,
            EmployeeRepository employeeRepository,
            CustomerRepository customerRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.cafeTableRepository = cafeTableRepository;
        this.productRepository = productRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
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
            if (item.productId() == null) {
                throw new BadRequestException("Mã món (productId) không được để trống");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new BadRequestException("Số lượng gọi món phải lớn hơn 0");
            }

            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món có id = " + item.productId()));

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

        PaymentMethod paymentMethod = (request != null && request.paymentMethod() != null)
                ? request.paymentMethod()
                : PaymentMethod.CASH;

        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setStatus(InvoiceStatus.PAID);

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
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new BadRequestException("Chỉ có thể chỉnh sửa món trên hóa đơn đang mở (OPEN)");
        }
        if (productId == null) {
            throw new BadRequestException("Mã món không được để trống");
        }

        InvoiceDetail detail = invoice.getDetails().stream()
                .filter(d -> d.getProduct() != null && d.getProduct().getId().equals(productId))
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

    private void applyDiscountInternal(
            Invoice invoice,
            String discountCode,
            BigDecimal discountAmount,
            Double discountPercent
    ) {
        BigDecimal subtotal = invoice.getSubtotal();
        BigDecimal calculatedDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (discountCode != null && !discountCode.isBlank()) {
            String code = discountCode.trim().toUpperCase(Locale.ROOT);
            switch (code) {
                case "GIAM10", "WELCOME" ->
                        calculatedDiscount = subtotal.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
                case "GIAM20" ->
                        calculatedDiscount = subtotal.multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);
                case "VIP" ->
                        calculatedDiscount = subtotal.multiply(BigDecimal.valueOf(0.15)).setScale(2, RoundingMode.HALF_UP);
                case "CAFE50K" ->
                        calculatedDiscount = BigDecimal.valueOf(50000).setScale(2, RoundingMode.HALF_UP);
                default ->
                        throw new BadRequestException("Mã giảm giá không hợp lệ: " + discountCode);
            }
        } else if (discountPercent != null) {
            if (discountPercent < 0 || discountPercent > 100) {
                throw new BadRequestException("Phần trăm giảm giá phải nằm trong khoảng từ 0% đến 100%");
            }
            calculatedDiscount = subtotal.multiply(BigDecimal.valueOf(discountPercent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (discountAmount != null) {
            if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Số tiền giảm giá không được âm");
            }
            calculatedDiscount = discountAmount.setScale(2, RoundingMode.HALF_UP);
        } else {
            throw new BadRequestException("Vui lòng cung cấp mã giảm giá, phần trăm hoặc số tiền chiết khấu");
        }

        // Không cho phép giảm giá vượt quá tổng tiền hàng (subtotal)
        if (calculatedDiscount.compareTo(subtotal) > 0) {
            calculatedDiscount = subtotal;
        }

        invoice.setDiscount(calculatedDiscount.setScale(2, RoundingMode.HALF_UP));
        BigDecimal totalAmount = subtotal.subtract(calculatedDiscount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        invoice.setTotalAmount(totalAmount);
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
