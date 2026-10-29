package com.example.BuiThiDiemTrinh_TH5.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BuiThiDiemTrinh_TH5.entity.QrGuestOrder;
public interface QrGuestOrderRepository extends JpaRepository<QrGuestOrder, Long> {
    Optional<QrGuestOrder> findByToken(String token);
    List<QrGuestOrder> findByStatusOrderByCreatedAtAsc(String status);
    List<QrGuestOrder> findByCafeTableIdAndStatus(Long tableId, String status);
}
