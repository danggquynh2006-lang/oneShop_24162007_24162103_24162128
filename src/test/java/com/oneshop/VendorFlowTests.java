package com.oneshop;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.BusinessException;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.promotion.PromotionService;
import com.oneshop.repository.ShopRepository;
import com.oneshop.repository.PromotionRepository;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ProductImageRepository;
import com.oneshop.repository.OrderRepository;
import com.oneshop.entity.ProductImage;
import com.oneshop.vendor.VendorProductService;
import com.oneshop.vendor.VendorOrderService;
import com.oneshop.vendor.VendorRevenueService;
import com.oneshop.vendor.ShopService;
import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import com.oneshop.repository.CategoryRepository;
import com.oneshop.vendor.ProductForm;

@SpringBootTest @ActiveProfiles("demo") @Transactional
class VendorFlowTests {
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PromotionService promotionService;
    @Autowired private ShopRepository shopRepository;
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private VendorRevenueService revenueService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductImageRepository productImageRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private VendorProductService productService;
    @Autowired private VendorOrderService orderService;
    @Autowired private ShopService shopService;

    @Test
    void shopsHaveCreationTimestampsAndAdminCanSetCommission() {
        var shop = shopRepository.findAll().get(0);
        assertNotNull(shop.getCreatedAt());

        shopService.updateCommission(shop.getId(), new BigDecimal("12.50"));

        assertEquals(0, new BigDecimal("12.50").compareTo(shop.getCommissionRate()));
        assertThrows(BusinessException.class,
                () -> shopService.updateCommission(shop.getId(), new BigDecimal("100.01")));
    }

    @Test
    void promotionCapsDiscountAndChecksShop() {
        Long shopId = shopRepository.findAll().get(0).getId();
        var discount = promotionService.calculate("LUMIERE10", shopId,
                new BigDecimal("600000"), new BigDecimal("22000"));
        assertEquals(0, new BigDecimal("50000").compareTo(discount.total()));
        assertThrows(BusinessException.class, () -> promotionService.calculate("LUMIERE10", shopId + 1,
                new BigDecimal("600000"), BigDecimal.ZERO));
        promotionService.redeem("LUMIERE10", shopId, new BigDecimal("600000"), BigDecimal.ZERO);
        assertEquals(1, promotionRepository.findByCodeIgnoreCase("LUMIERE10").orElseThrow().getUsed());
    }

    @Test
    void revenueUsesDeliveredOrdersAndCommission() {
        Long shopId = shopRepository.findAll().get(0).getId();
        LocalDate today = LocalDate.now();
        var summary = revenueService.summary(shopId, today.getYear(), today.getMonthValue());
        assertEquals(1, summary.orders());
        assertEquals(0, new BigDecimal("345000").compareTo(summary.gross()));
        assertEquals(0, new BigDecimal("27600").compareTo(summary.commission()));
        assertEquals(0, new BigDecimal("317400").compareTo(summary.net()));
        assertEquals(1, revenueService.annual(shopId, today.getYear()).orders());
        assertEquals(1, revenueService.daily(shopId, today.minusDays(2)).orders());
    }

    @Test
    void vendorCannotReadAnotherShopsProductsOrOrders() {
        Long otherShopId = shopRepository.findAll().stream().filter(shop -> !shop.getName().equals("Lumière Beauty"))
                .findFirst().orElseThrow().getId();
        Long productId = productRepository.findAll().get(0).getId();
        Long orderId = orderRepository.findAll().get(0).getId();
        assertThrows(ResourceNotFoundException.class, () -> productService.get(otherShopId, productId));
        assertThrows(ResourceNotFoundException.class, () -> orderService.get(otherShopId, orderId));
    }
    @Test
    void vendorCanCreateProductWithDiscount() throws Exception {
        Long shopId = shopRepository.findAll().get(0).getId();
        Category category = categoryRepository.findAll().stream().filter(Category::isActive)
                .findFirst().orElseThrow();
        ProductForm form = new ProductForm();
        form.setName("Má hồng thử nghiệm");
        form.setPrice(new BigDecimal("200000"));
        form.setStock(5);
        form.setCategoryId(category.getId());
        form.setDiscountPercent(25);

        Product saved = productService.create(shopId, form, null);

        assertEquals(25, productService.get(shopId, saved.getId()).getDiscountPercent());
        assertEquals(0, new BigDecimal("150000").compareTo(saved.getFinalPrice()));
    }

    @Test
    void deletingMainProductImagePromotesAnotherImage() {
        Long shopId = shopRepository.findAll().stream().filter(shop -> shop.getName().equals("Lumière Beauty"))
                .findFirst().orElseThrow().getId();
        var product = productRepository.findByShopIdAndNameContainingIgnoreCase(
                shopId, "", org.springframework.data.domain.PageRequest.of(0, 1)).getContent().get(0);
        var mainImage = productImageRepository.save(ProductImage.builder().product(product)
                .url("https://example.test/main.jpg").mainImage(true).build());
        productImageRepository.save(ProductImage.builder().product(product)
                .url("https://example.test/secondary.jpg").mainImage(false).build());

        productService.removeImage(shopId, product.getId(), mainImage.getId());

        var remaining = productImageRepository.findByProductIdOrderByMainImageDescIdAsc(product.getId());
        assertEquals(1, remaining.stream().filter(ProductImage::isMainImage).count());
        assertTrue(remaining.get(0).isMainImage());
    }
}
