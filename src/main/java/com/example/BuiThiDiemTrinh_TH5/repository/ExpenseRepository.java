package com.example.BuiThiDiemTrinh_TH5.repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.BuiThiDiemTrinh_TH5.entity.Expense;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.paymentMethod = :method and e.createdAt >= :from and e.createdAt <= :to")
    BigDecimal totalByPaymentBetween(@Param("method") PaymentMethod method, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

}
