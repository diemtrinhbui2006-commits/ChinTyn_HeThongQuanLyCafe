package com.example.BuiThiDiemTrinh_TH5.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import com.example.BuiThiDiemTrinh_TH5.entity.Promotion;
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCodeIgnoreCase(String code);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Promotion p where upper(p.code) = upper(:code)")
    Optional<Promotion> findByCodeIgnoreCaseForUpdate(@Param("code") String code);
}
