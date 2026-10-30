package com.example.BuiThiDiemTrinh_TH5.service;

import java.util.List;

import com.example.BuiThiDiemTrinh_TH5.entity.Product;

public interface ProductService {


    Product create(Product product);

    Product update(Long id, Product product);

    Product setAvailability(Long id, boolean available);
    void delete(Long id);


    List<Product> getAllProducts();

    List<Product> searchProductsByName(String keyword);


    Product getProductById(Long id);
}
