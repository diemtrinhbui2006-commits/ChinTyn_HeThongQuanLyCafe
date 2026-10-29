package com.example.BuiThiDiemTrinh_TH5.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "work_shifts")
@Data @NoArgsConstructor
public class WorkShift {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Employee employee;
    @Column(nullable = false)
    private LocalDateTime openedAt = LocalDateTime.now();
    private LocalDateTime closedAt;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal openingCash;
    @Column(precision = 14, scale = 2)
    private BigDecimal closingCash;
    @Column(precision = 14, scale = 2)
    private BigDecimal expectedCash;
    @Column(length = 500)
    private String note;
}
