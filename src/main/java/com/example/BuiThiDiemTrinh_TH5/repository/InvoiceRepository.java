package com.example.BuiThiDiemTrinh_TH5.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;

import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceStatus;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findFirstByCafeTableIdAndStatus(Long tableId, InvoiceStatus status);

    List<Invoice> findByStatus(InvoiceStatus status);
    List<Invoice> findByStatusAndPaidAtGreaterThanEqualAndPaidAtLessThanEqual(
            InvoiceStatus status, LocalDateTime from, LocalDateTime to);

    List<Invoice> findByCafeTableId(Long tableId);

    Optional<Invoice> findByIdAndStatus(Long id, InvoiceStatus status);

    boolean existsByCafeTableIdAndStatus(Long tableId, InvoiceStatus status);

    @Query("select coalesce(sum(i.totalAmount), 0) from Invoice i where i.status = com.example.BuiThiDiemTrinh_TH5.entity.InvoiceStatus.PAID and i.paymentMethod = :method and i.paidAt >= :from and i.paidAt <= :to")
    BigDecimal totalPaidByMethodBetween(@Param("method") PaymentMethod method, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

}
