package com.example.BuiThiDiemTrinh_TH5.dto;

public record UserRequest(
        String username,
        String password,
        String role,
        Long employeeId
) {
}
