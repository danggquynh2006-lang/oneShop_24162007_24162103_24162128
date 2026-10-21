package com.oneshop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.entity.Category;
import com.oneshop.repository.CategoryRepository;
import com.oneshop.repository.ProductImageRepository;
import com.oneshop.repository.ProductRepository;

@SpringBootTest @ActiveProfiles("demo") @Transactional
class DemoCatalogTests {
    @Autowired private CategoryRepository categories;
    @Autowired private ProductRepository products;
    @Autowired private ProductImageRepository images;

    @Test
    void highEndMakeupHasSixProductsPerCategory() {
        Category makeup = categories.findFirstByNameIgnoreCaseAndParentIsNull("Trang điểm").orElseThrow();
        for (String categoryName : new String[] {"Má hồng", "Son môi", "Bảng mắt"}) {
            Category category = categories.findFirstByNameIgnoreCaseAndParentId(categoryName, makeup.getId())
                    .orElseThrow();
            var catalog = products.findAll().stream()
                    .filter(product -> product.getCategory().getId().equals(category.getId())).toList();
            assertEquals(6, catalog.size(), categoryName);
            assertTrue(catalog.stream().allMatch(product -> product.isActive()
                    && product.getPrice().compareTo(BigDecimal.ZERO) > 0
                    && product.getStock() > 0 && !product.getBrand().isBlank()), categoryName);
            assertTrue(catalog.stream().allMatch(product -> {
                var productImages = images.findByProductIdOrderByMainImageDescIdAsc(product.getId());
                return productImages.size() == 1 && productImages.get(0).isMainImage()
                        && productImages.get(0).getUrl().startsWith("https://");
            }), categoryName);
        }
        assertEquals(18, images.count());
    }
}
