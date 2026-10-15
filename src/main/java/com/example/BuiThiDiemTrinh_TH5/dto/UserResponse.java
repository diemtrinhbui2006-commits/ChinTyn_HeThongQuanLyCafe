package com.example.BuiThiDiemTrinh_TH5.dto;

public record UserResponse(
        Long id,
        String username,
        String role,
        Long employeeId
) {
}
