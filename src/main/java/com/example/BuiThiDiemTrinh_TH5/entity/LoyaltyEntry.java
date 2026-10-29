package com.example.BuiThiDiemTrinh_TH5.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loyalty_entries")
@Data @NoArgsConstructor
public class LoyaltyEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Customer customer;
    @ManyToOne
    private Invoice invoice;
    @Column(nullable = false)
    private Integer points;
    @Column(nullable = false, length = 20)
    private String entryType;
    @Column(length = 250)
    private String description;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
