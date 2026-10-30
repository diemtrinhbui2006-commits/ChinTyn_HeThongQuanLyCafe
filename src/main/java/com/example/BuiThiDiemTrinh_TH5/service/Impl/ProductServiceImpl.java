package com.example.BuiThiDiemTrinh_TH5.service.Impl;

import java.math.BigDecimal;
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
        product.setCategory(getCategoryReference(product));
        product.setId(null);
        product.setDeleted(false);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(Long id, Product product) {
        validateProduct(product);
        Product existing = getProductById(id);
        existing.setName(product.getName().trim());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setQuantity(product.getQuantity());
        existing.setCategory(getCategoryReference(product));
        return productRepository.save(existing);
    }

    @Override
    @Transactional
    public Product setAvailability(Long id, boolean available) {
        Product product = getProductById(id);
        product.setAvailable(available);
        return productRepository.save(product);
    }


    @Override
    public List<Product> getAllProducts() {
        return productRepository.findByDeletedFalse();
    }

    @Override
    public List<Product> searchProductsByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BadRequestException("Từ khóa tìm kiếm không được để trống");
        }
        return productRepository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword.trim());
    }


    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .filter(product -> !product.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có id = " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = getProductById(id);
        product.setDeleted(true);
        product.setAvailable(false);
        productRepository.save(product);
    }

    private void validateProduct(Product product) {
        if (product == null || product.getName() == null || product.getName().isBlank()) {
            throw new BadRequestException("Tên sản phẩm không được để trống");
        }
        if (product.getName().length() > 100) {
            throw new BadRequestException("Tên sản phẩm không được vượt quá 100 ký tự");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Giá sản phẩm không hợp lệ");
        }
        if (product.getQuantity() == null || product.getQuantity() < 0) {
            throw new BadRequestException("Số lượng sản phẩm không hợp lệ");
        }
        if (product.getDescription() != null && product.getDescription().length() > 500) {
            throw new BadRequestException("Mô tả sản phẩm không được vượt quá 500 ký tự");
        }
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new BadRequestException("Danh mục sản phẩm không được để trống");
        }
    }

    private Category getCategoryReference(Product product) {
        Long categoryId = product.getCategory().getId();
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục có id = " + categoryId);
        }
        return entityManager.getReference(Category.class, categoryId);
    }
}
