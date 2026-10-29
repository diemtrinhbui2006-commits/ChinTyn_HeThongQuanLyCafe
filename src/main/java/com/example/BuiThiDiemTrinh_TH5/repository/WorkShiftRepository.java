package com.example.BuiThiDiemTrinh_TH5.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BuiThiDiemTrinh_TH5.entity.WorkShift;
public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
    Optional<WorkShift> findFirstByEmployeeIdAndClosedAtIsNullOrderByOpenedAtDesc(Long employeeId);
    List<WorkShift> findAllByOrderByOpenedAtDesc();
}
