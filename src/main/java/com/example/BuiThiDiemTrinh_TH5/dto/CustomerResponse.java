package com.example.BuiThiDiemTrinh_TH5.dto;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address
) {
}
