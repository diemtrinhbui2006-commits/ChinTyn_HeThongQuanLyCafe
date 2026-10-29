package com.example.BuiThiDiemTrinh_TH5.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ingredients")
@Data
@NoArgsConstructor
public class Ingredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 30)
    private String unit;
    @Column(nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal lowStockThreshold = BigDecimal.ZERO;
}
