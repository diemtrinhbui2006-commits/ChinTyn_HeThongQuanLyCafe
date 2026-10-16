package com.example.BuiThiDiemTrinh_TH5.dto;

import java.math.BigDecimal;

public record ApplyDiscountRequest(
        String discountCode,
        BigDecimal discountAmount,
        Double discountPercent
) {
}
