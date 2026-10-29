package com.example.BuiThiDiemTrinh_TH5.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BuiThiDiemTrinh_TH5.entity.LoyaltyReward;
public interface LoyaltyRewardRepository extends JpaRepository<LoyaltyReward, Long> {
    List<LoyaltyReward> findByActiveTrueOrderByPointsCostAsc();
}
