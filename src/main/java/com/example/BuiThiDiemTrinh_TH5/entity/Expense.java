package com.example.BuiThiDiemTrinh_TH5.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expenses")
@Data @NoArgsConstructor
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String description;
    @Column(nullable = false, length = 80)
    private String category;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    @ManyToOne(fetch = FetchType.LAZY)
    private Supplier supplier;
    @ManyToOne(fetch = FetchType.LAZY)
    private Ingredient ingredient;
    @Column(precision = 14, scale = 3)
    private BigDecimal stockQuantity;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
