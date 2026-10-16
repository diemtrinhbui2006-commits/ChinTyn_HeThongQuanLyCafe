package com.example.BuiThiDiemTrinh_TH5.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BuiThiDiemTrinh_TH5.dto.ProductRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.ProductResponse;
import com.example.BuiThiDiemTrinh_TH5.entity.Product;
import com.example.BuiThiDiemTrinh_TH5.mapper.ProductMapper;
import com.example.BuiThiDiemTrinh_TH5.service.ProductService;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    // Chức năng 1: Xem danh sách sản phẩm
    // GET: http://localhost:8080/api/products
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @GetMapping("/search")
    public List<ProductResponse> searchProductsByName(@RequestParam String keyword) {
        return productService.searchProductsByName(keyword)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    // Chức năng 2: Thêm sản phẩm
    // POST: http://localhost:8080/api/products
    @PostMapping
    public ProductResponse addProduct(@RequestBody ProductRequest request) {
        return productMapper.toResponse(productService.create(productMapper.toEntity(request)));
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request
    ) {
        return productMapper.toResponse(productService.update(id, productMapper.toEntity(request)));
    }

    @PatchMapping("/{id}/availability")
    public ProductResponse setProductAvailability(
            @PathVariable Long id,
            @RequestParam boolean available
    ) {
        return productMapper.toResponse(productService.setAvailability(id, available));
    }

    // Chức năng 3: Xem chi tiết sản phẩm theo ID
    // GET: http://localhost:8080/api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {

        Product product = productService.getProductById(id);

        ProductResponse response = productMapper.toResponse(product);

        return ResponseEntity.ok(response);
    }
}