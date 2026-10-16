package com.example.BuiThiDiemTrinh_TH5.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceStatus;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;

public record InvoiceResponse(
        Long id,
        Long tableId,
        Integer tableNumber,
        Long employeeId,
        String employeeName,
        Long customerId,
        String customerName,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal totalAmount,
        InvoiceStatus status,
        PaymentMethod paymentMethod,
        List<InvoiceDetailResponse> details
) {
}
