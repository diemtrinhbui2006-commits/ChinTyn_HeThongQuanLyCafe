package com.example.BuiThiDiemTrinh_TH5.repository;

import com.example.BuiThiDiemTrinh_TH5.entity.Category;

import java.util.List;

public interface CategoryRepository {

    Category save(Category category);

    List<Category> findAll();

    boolean existsById(Long id);
}