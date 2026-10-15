package com.example.BuiThiDiemTrinh_TH5.service.Impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.BuiThiDiemTrinh_TH5.entity.Category;
import com.example.BuiThiDiemTrinh_TH5.entity.Product;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.repository.CategoryRepository;
import com.example.BuiThiDiemTrinh_TH5.repository.ProductRepository;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void updateChangesProductDetailsAndPrice() {
        Category category = new Category();
        category.setId(3L);
        Product existing = new Product();
        existing.setId(7L);
        when(productRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsById(3L)).thenReturn(true);
        when(entityManager.getReference(Category.class, 3L)).thenReturn(category);
        when(productRepository.save(existing)).thenReturn(existing);

        Product updated = productService.update(7L, new Product(
                null, "Latte", "Ca phe sua", 45000.0, 10, true, category
        ));

        assertSame(existing, updated);
        assertEquals(7L, updated.getId());
        assertEquals("Latte", updated.getName());
        assertEquals(45000.0, updated.getPrice());
        assertSame(category, updated.getCategory());
        verify(productRepository).save(existing);
    }

    @Test
    void setAvailabilityCanStopSellingProduct() {
        Product existing = new Product();
        existing.setId(7L);
        when(productRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        Product updated = productService.setAvailability(7L, false);

        assertFalse(updated.isAvailable());
        verify(productRepository).save(existing);
    }

    @Test
    void updateRejectsNonPositivePrice() {
        Product invalid = new Product();
        invalid.setName("Latte");
        invalid.setPrice(0.0);

        assertThrows(BadRequestException.class, () -> productService.update(7L, invalid));
    }
}
