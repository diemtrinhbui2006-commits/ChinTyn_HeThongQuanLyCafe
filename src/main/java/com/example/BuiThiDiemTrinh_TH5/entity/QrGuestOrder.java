package com.example.BuiThiDiemTrinh_TH5.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qr_guest_orders")
@Data @NoArgsConstructor
public class QrGuestOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String token;
    @ManyToOne(optional = false)
    private CafeTable cafeTable;
    @Column(nullable = false, length = 20)
    private String status = "PENDING";
    @Column(nullable = false, length = 4000)
    private String itemsJson;
    @Column(length = 100)
    private String guestName;
    @Column(length = 20)
    private String guestPhone;
    @ManyToOne
    private Invoice approvedInvoice;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
