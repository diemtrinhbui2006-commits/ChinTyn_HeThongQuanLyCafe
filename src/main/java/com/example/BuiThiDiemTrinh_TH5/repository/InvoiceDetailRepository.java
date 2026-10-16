package com.example.BuiThiDiemTrinh_TH5.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceDetail;

public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {

    Optional<InvoiceDetail> findByInvoiceIdAndProductId(Long invoiceId, Long productId);

    List<InvoiceDetail> findByInvoiceId(Long invoiceId);

    void deleteByInvoiceIdAndProductId(Long invoiceId, Long productId);
}
