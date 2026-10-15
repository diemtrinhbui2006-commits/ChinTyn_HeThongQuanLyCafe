package com.example.BuiThiDiemTrinh_TH5.service;

import java.util.List;

import com.example.BuiThiDiemTrinh_TH5.entity.Product;

public interface ProductService {


    Product create(Product product);


    List<Product> getAllProducts();

    List<Product> searchProductsByName(String keyword);


    Product getProductById(Long id);
}