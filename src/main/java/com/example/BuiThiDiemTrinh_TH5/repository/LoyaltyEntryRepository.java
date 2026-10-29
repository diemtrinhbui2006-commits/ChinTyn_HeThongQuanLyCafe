package com.example.BuiThiDiemTrinh_TH5.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BuiThiDiemTrinh_TH5.entity.LoyaltyEntry;
public interface LoyaltyEntryRepository extends JpaRepository<LoyaltyEntry, Long> {
    List<LoyaltyEntry> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    boolean existsByInvoiceIdAndEntryType(Long invoiceId, String entryType);
}
