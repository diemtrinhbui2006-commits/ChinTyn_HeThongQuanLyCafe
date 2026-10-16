package com.example.BuiThiDiemTrinh_TH5.gui.binding;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.dto.CheckoutRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.InvoiceResponse;
import com.example.BuiThiDiemTrinh_TH5.dto.OrderFoodRequest;
import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.mapper.InvoiceMapper;
import com.example.BuiThiDiemTrinh_TH5.service.InvoiceService;

/**
 * Cầu nối giữa giao diện Swing/JavaFX và nghiệp vụ hóa đơn của Tuần 3.
 *
 * Giao diện chỉ cần gọi {@link #addFood(AddFoodFormData)} khi bấm nút
 * "Thêm món" và {@link #checkout(CheckoutFormData)} khi bấm nút
 * "Thanh toán". Mọi thay đổi hóa đơn, tồn kho và trạng thái bàn vẫn được
 * xử lý tập trung bởi {@link InvoiceService}.
 */
@Component
public class InvoiceGuiBinding {

    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;

    public InvoiceGuiBinding(InvoiceService invoiceService, InvoiceMapper invoiceMapper) {
        this.invoiceService = invoiceService;
        this.invoiceMapper = invoiceMapper;
    }

    /**
     * Data Binding cho nút "Thêm món".
     */
    @Transactional
    public InvoiceResponse addFood(AddFoodFormData formData) {
        if (formData == null) {
            throw new BadRequestException("Dữ liệu gọi món không được để trống");
        }

        OrderFoodRequest request = new OrderFoodRequest(
                parseRequiredLong(formData.tableId(), "Mã bàn"),
                parseOptionalLong(formData.employeeId(), "Mã nhân viên"),
                parseOptionalLong(formData.customerId(), "Mã khách hàng"),
                parseRequiredLong(formData.productId(), "Mã món"),
                parseRequiredInteger(formData.quantity(), "Số lượng"),
                null
        );

        Invoice invoice = invoiceService.orderFood(request);
        return invoiceMapper.toResponse(invoice);
    }

    /**
     * Data Binding cho nút "Thanh toán".
     */
    @Transactional
    public InvoiceResponse checkout(CheckoutFormData formData) {
        if (formData == null) {
            throw new BadRequestException("Dữ liệu thanh toán không được để trống");
        }

        Long invoiceId = parseRequiredLong(formData.invoiceId(), "Mã hóa đơn");
        CheckoutRequest request = new CheckoutRequest(
                parsePaymentMethod(formData.paymentMethod()),
                trimToNull(formData.discountCode()),
                parseOptionalBigDecimal(formData.discountAmount(), "Số tiền giảm"),
                parseOptionalDouble(formData.discountPercent(), "Phần trăm giảm")
        );

        Invoice invoice = invoiceService.checkout(invoiceId, request);
        return invoiceMapper.toResponse(invoice);
    }

    /**
     * Nạp lại hóa đơn đang mở để giao diện hiển thị danh sách món và tổng tiền.
     */
    @Transactional(readOnly = true)
    public InvoiceResponse loadOpenInvoice(String tableId) {
        Invoice invoice = invoiceService.getOpenInvoiceByTableId(
                parseRequiredLong(tableId, "Mã bàn")
        );
        return invoiceMapper.toResponse(invoice);
    }

    private Long parseRequiredLong(String value, String fieldName) {
        String normalized = requireText(value, fieldName);
        try {
            long parsed = Long.parseLong(normalized);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new BadRequestException(fieldName + " phải là số nguyên dương");
        }
    }

    private Long parseOptionalLong(String value, String fieldName) {
        return trimToNull(value) == null ? null : parseRequiredLong(value, fieldName);
    }

    private Integer parseRequiredInteger(String value, String fieldName) {
        String normalized = requireText(value, fieldName);
        try {
            int parsed = Integer.parseInt(normalized);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new BadRequestException(fieldName + " phải là số nguyên dương");
        }
    }

    private BigDecimal parseOptionalBigDecimal(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            throw new BadRequestException(fieldName + " không đúng định dạng số");
        }
    }

    private Double parseOptionalDouble(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Double.valueOf(normalized.replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new BadRequestException(fieldName + " không đúng định dạng số");
        }
    }

    private PaymentMethod parsePaymentMethod(String value) {
        String normalized = normalizeLabel(value);
        if (normalized == null) {
            return PaymentMethod.CASH;
        }

        return switch (normalized) {
            case "CASH", "TIEN_MAT" -> PaymentMethod.CASH;
            case "BANK_TRANSFER", "CHUYEN_KHOAN" -> PaymentMethod.BANK_TRANSFER;
            case "CARD", "THE" -> PaymentMethod.CARD;
            case "E_WALLET", "VI_DIEN_TU" -> PaymentMethod.E_WALLET;
            default -> throw new BadRequestException("Phương thức thanh toán không hợp lệ: " + value);
        };
    }

    private String normalizeLabel(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        return Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String requireText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BadRequestException(fieldName + " không được để trống");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
