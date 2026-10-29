package com.example.BuiThiDiemTrinh_TH5.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suppliers")
@Data @NoArgsConstructor
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(length = 100)
    private String contactName;
    @Column(length = 30)
    private String phone;
    @Column(length = 150)
    private String email;
    @Column(length = 500)
    private String address;
}
