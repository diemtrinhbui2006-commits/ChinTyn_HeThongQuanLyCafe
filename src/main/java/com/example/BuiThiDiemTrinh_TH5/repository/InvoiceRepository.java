package com.example.BuiThiDiemTrinh_TH5.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
