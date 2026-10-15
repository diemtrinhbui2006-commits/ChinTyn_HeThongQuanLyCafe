package com.example.BuiThiDiemTrinh_TH5.dto;

public record ProductRequest(
        String name,
        String description,
        Double price,
        Integer quantity,
        Long categoryId
) {
}