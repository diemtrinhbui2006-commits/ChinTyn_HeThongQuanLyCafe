package com.example.BuiThiDiemTrinh_TH5.dto;

import java.math.BigDecimal;

public record EmployeeRequest(
        String name,
        String email,
        String phone,
        String position,
        BigDecimal salary
) {
}
