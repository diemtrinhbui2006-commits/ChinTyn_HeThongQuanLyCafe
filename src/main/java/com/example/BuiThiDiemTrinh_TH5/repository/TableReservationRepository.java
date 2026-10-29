package com.example.BuiThiDiemTrinh_TH5.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BuiThiDiemTrinh_TH5.entity.TableReservation;
public interface TableReservationRepository extends JpaRepository<TableReservation, Long> {
    List<TableReservation> findAllByOrderByReservedForAsc();
    boolean existsByCafeTableIdAndStatus(Long tableId, String status);
}
