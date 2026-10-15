package com.example.BuiThiDiemTrinh_TH5.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BuiThiDiemTrinh_TH5.entity.CafeTable;
import com.example.BuiThiDiemTrinh_TH5.entity.TableStatus;

public interface CafeTableRepository extends JpaRepository<CafeTable, Long> {

    List<CafeTable> findByStatus(TableStatus status);
}
