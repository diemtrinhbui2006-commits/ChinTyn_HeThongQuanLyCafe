package com.example.BuiThiDiemTrinh_TH5.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_movements")
@Data @NoArgsConstructor
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Ingredient ingredient;
    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;
    @Column(nullable = false, length = 20)
    private String movementType;
    @Column(length = 500)
    private String note;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
