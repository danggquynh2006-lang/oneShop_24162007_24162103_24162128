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
import com.oneshop.repository.OrderRepository;
import com.oneshop.vendor.VendorProductService;
import com.oneshop.vendor.VendorOrderService;
import com.oneshop.vendor.VendorRevenueService;

@SpringBootTest @ActiveProfiles("demo") @Transactional
class VendorFlowTests {
    @Autowired private PromotionService promotionService;
    @Autowired private ShopRepository shopRepository;
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private VendorRevenueService revenueService;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private VendorProductService productService;
    @Autowired private VendorOrderService orderService;

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
}
