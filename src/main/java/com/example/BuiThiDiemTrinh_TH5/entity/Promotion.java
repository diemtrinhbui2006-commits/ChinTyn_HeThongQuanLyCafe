package com.example.BuiThiDiemTrinh_TH5.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promotions")
@Data @NoArgsConstructor
public class Promotion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 12)
    private String discountType = "PERCENT";
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal discountValue;
    @Column(precision = 14, scale = 2)
    private BigDecimal minimumSpend;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    @Column(nullable = false)
    private boolean active = true;
    private Integer usageLimit;
    @Column(nullable = false)
    private Integer usageCount = 0;
}
