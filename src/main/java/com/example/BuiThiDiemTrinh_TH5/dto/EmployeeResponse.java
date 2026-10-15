package com.example.BuiThiDiemTrinh_TH5.dto;

public record EmployeeResponse(
        Long id,
        String name,
        String email,
        String phone,
        String position,
        Double salary
) {
}
