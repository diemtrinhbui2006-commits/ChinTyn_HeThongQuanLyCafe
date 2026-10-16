package com.example.BuiThiDiemTrinh_TH5.gui.binding;

/**
 * Dữ liệu lấy trực tiếp từ màn hình thanh toán.
 */
public record CheckoutFormData(
        String invoiceId,
        String paymentMethod,
        String discountCode,
        String discountAmount,
        String discountPercent
) {
}
