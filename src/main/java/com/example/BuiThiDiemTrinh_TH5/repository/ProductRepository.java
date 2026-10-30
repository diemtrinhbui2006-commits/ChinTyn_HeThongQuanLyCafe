package com.example.BuiThiDiemTrinh_TH5.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BuiThiDiemTrinh_TH5.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByDeletedFalse();
    List<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String keyword);
}
