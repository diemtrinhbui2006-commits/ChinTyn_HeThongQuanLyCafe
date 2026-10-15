package com.example.BuiThiDiemTrinh_TH5.dto;
public record ProductResponse(
        Long id,
        String name,
        String description,
        Double price,
        Integer quantity,
        Long categoryId,
        boolean available
) {
}