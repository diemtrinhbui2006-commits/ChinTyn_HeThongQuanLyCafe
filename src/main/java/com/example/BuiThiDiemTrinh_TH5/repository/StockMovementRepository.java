package com.example.BuiThiDiemTrinh_TH5.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BuiThiDiemTrinh_TH5.entity.StockMovement;
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByIngredientIdOrderByCreatedAtDesc(Long ingredientId);
}
