package com.example.BuiThiDiemTrinh_TH5.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceStatus;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findFirstByCafeTableIdAndStatus(Long tableId, InvoiceStatus status);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByCafeTableId(Long tableId);

    Optional<Invoice> findByIdAndStatus(Long id, InvoiceStatus status);

    boolean existsByCafeTableIdAndStatus(Long tableId, InvoiceStatus status);
}
