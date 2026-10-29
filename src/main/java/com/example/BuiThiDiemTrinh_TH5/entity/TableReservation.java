package com.example.BuiThiDiemTrinh_TH5.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "table_reservations")
@Data @NoArgsConstructor
public class TableReservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private CafeTable cafeTable;
    @Column(nullable = false, length = 100)
    private String customerName;
    @Column(length = 20)
    private String phone;
    @Column(nullable = false)
    private LocalDateTime reservedFor;
    @Column(nullable = false)
    private Integer partySize;
    @Column(nullable = false, length = 20)
    private String status = "BOOKED";
    @Column(length = 500)
    private String note;
}
