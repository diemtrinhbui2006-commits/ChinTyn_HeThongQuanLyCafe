package com.example.BuiThiDiemTrinh_TH5.service;

import java.util.List;

import com.example.BuiThiDiemTrinh_TH5.entity.CafeTable;

public interface CafeTableService {

    CafeTable create(CafeTable cafeTable);

    List<CafeTable> getTables(String status);

    CafeTable getTableById(Long id);

    CafeTable update(Long id, CafeTable cafeTable);

    void delete(Long id);
}
