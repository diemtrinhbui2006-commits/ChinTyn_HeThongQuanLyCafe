package com.example.BuiThiDiemTrinh_TH5.service;

import java.util.List;

import com.example.BuiThiDiemTrinh_TH5.entity.Category;

public interface CategoryService {
    Category create(Category category);

    List<Category> getAllCategories();
}