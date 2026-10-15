package com.example.BuiThiDiemTrinh_TH5.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.entity.Category;
import com.example.BuiThiDiemTrinh_TH5.entity.Product;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.exception.ResourceNotFoundException;
import com.example.BuiThiDiemTrinh_TH5.repository.CategoryRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.ProductRepository;
import com.example.BuiThiDiemTrinh_TH5.service.ProductService;

import jakarta.persistence.EntityManager;

@Service
public class ProductServiceImpl implements ProductService {


    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;


    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            EntityManager entityManager
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.entityManager = entityManager;
    }


    @Override
    @Transactional
    public Product create(Product product) {
        validateProduct(product);
        Long categoryId = product.getCategory().getId();
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy danh mục có id = " + categoryId
            );
        }

        product.setCategory(entityManager.getReference(Category.class, categoryId));
        product.setId(null);
        return productRepository.save(product);
    }


    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có id = " + id));
    }

    private void validateProduct(Product product) {
        if (product == null || product.getName() == null || product.getName().isBlank()) {
            throw new BadRequestException("Tên sản phẩm không được để trống");
        }
        if (product.getPrice() == null || product.getPrice() < 0) {
            throw new BadRequestException("Giá sản phẩm không hợp lệ");
        }
        if (product.getQuantity() == null || product.getQuantity() < 0) {
            throw new BadRequestException("Số lượng sản phẩm không hợp lệ");
        }
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new BadRequestException("Danh mục sản phẩm không được để trống");
        }
    }
}