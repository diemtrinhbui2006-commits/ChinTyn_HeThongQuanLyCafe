package com.example.BuiThiDiemTrinh_TH5.dto;

import java.util.List;

public record OrderFoodRequest(
        Long tableId,
        Long employeeId,
        Long customerId,
        Long productId,
        Integer quantity,
        List<OrderItemRequest> items
) {
}
