package com.example.BuiThiDiemTrinh_TH5.dto;

import java.math.BigDecimal;

import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;

public record CheckoutRequest(
        PaymentMethod paymentMethod,
        String discountCode,
        BigDecimal discountAmount,
        Double discountPercent
) {
}
