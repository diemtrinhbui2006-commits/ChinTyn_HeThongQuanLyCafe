package com.example.BuiThiDiemTrinh_TH5.dto;

public record OrderItemRequest(
        Long productId,
        Integer quantity,
        Integer sugarPercent,
        String iceOption,
        String note
) {
    public OrderItemRequest(Long productId, Integer quantity) {
        this(productId, quantity, 100, "NORMAL", "");
    }
}
