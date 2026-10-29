package com.example.BuiThiDiemTrinh_TH5.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loyalty_rewards")
@Data @NoArgsConstructor
public class LoyaltyReward {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(nullable = false)
    private Integer pointsCost;
    @Column(nullable = false)
    private boolean active = true;
}
