package com.example.BuiThiDiemTrinh_TH5.dto;

import java.math.BigDecimal;

public record InvoiceDetailResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
