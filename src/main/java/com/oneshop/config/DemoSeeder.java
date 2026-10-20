package com.oneshop.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.oneshop.common.OrderStatus;
import com.oneshop.common.PaymentMethod;
import com.oneshop.common.PaymentStatus;
import com.oneshop.common.ShopStatus;
import com.oneshop.common.PromotionScope;
import com.oneshop.common.PromotionType;
import com.oneshop.entity.*;
import com.oneshop.entity.Order;
import com.oneshop.repository.*;
import lombok.RequiredArgsConstructor;

@Component @Profile("demo") @org.springframework.core.annotation.Order(100) @RequiredArgsConstructor
public class DemoSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final ShopRepository shops;
    private final ProductRepository products;
    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final PaymentRepository payments;
    private final PromotionRepository promotions;

    @Override
    public void run(String... args) {
        if (shops.count() > 0) return;
        User owner = users.findByEmail("vendor@oneshop.com").orElseThrow();
        User buyer = users.findByEmail("user@oneshop.com").orElseThrow();
        Category category = categories.findAll().get(0);
        Shop shop = shops.save(Shop.builder().owner(owner).name("Lumière Beauty")
                .description("Mỹ phẩm chăm sóc da dịu nhẹ, chọn lọc cho mỗi ngày.")
                .address("12 Nguyễn Huệ, Quận 1, TP.HCM").status(ShopStatus.ACTIVE)
                .commissionRate(new BigDecimal("8.00")).build());
        shops.save(Shop.builder().owner(buyer).name("Mộc Beauty")
                .description("Hồ sơ shop mẫu đang chờ duyệt.")
                .address("42 Hai Bà Trưng, Quận 1, TP.HCM").status(ShopStatus.PENDING).build());
        List<Product> samples = products.saveAll(List.of(
                Product.builder().shop(shop).category(category).name("Serum phục hồi da")
                        .brand("Lumière").description("Serum cấp ẩm và phục hồi hàng rào bảo vệ da.")
                        .price(new BigDecimal("289000")).stock(50).sold(24).build(),
                Product.builder().shop(shop).category(category).name("Kem dưỡng ban đêm")
                        .brand("Lumière").description("Kem dưỡng mềm mịn cho da khô.")
                        .price(new BigDecimal("345000")).stock(30).sold(18).build(),
                Product.builder().shop(shop).category(category).name("Sữa rửa mặt dịu nhẹ")
                        .brand("Lumière").description("Làm sạch nhẹ nhàng mỗi ngày.")
                        .price(new BigDecimal("179000")).stock(42).sold(31).build()));
        Order order = orders.save(com.oneshop.entity.Order.builder().user(buyer).shop(shop)
                .addressSnapshot("12 Lê Lợi, Quận 1, TP.HCM").status(OrderStatus.NEW)
                .subtotal(new BigDecimal("578000")).shippingFee(new BigDecimal("22000"))
                .total(new BigDecimal("600000")).paymentMethod(PaymentMethod.COD).build());
        items.save(OrderItem.builder().order(order).product(samples.get(0)).productName(samples.get(0).getName())
                .unitPrice(samples.get(0).getPrice()).quantity(2).build());
        payments.save(Payment.builder().order(order).method(PaymentMethod.COD)
                .amount(order.getTotal()).status(PaymentStatus.UNPAID).build());
        Order delivered = orders.save(com.oneshop.entity.Order.builder().user(buyer).shop(shop)
                .addressSnapshot("12 Lê Lợi, Quận 1, TP.HCM").status(OrderStatus.DELIVERED)
                .subtotal(new BigDecimal("345000")).shippingFee(new BigDecimal("22000"))
                .total(new BigDecimal("367000")).paymentMethod(PaymentMethod.COD)
                .commissionRateSnapshot(new BigDecimal("8.00"))
                .deliveredAt(LocalDateTime.now().minusDays(2)).build());
        items.save(OrderItem.builder().order(delivered).product(samples.get(1)).productName(samples.get(1).getName())
                .unitPrice(samples.get(1).getPrice()).quantity(1).build());
        payments.save(Payment.builder().order(delivered).method(PaymentMethod.COD)
                .amount(delivered.getTotal()).status(PaymentStatus.PAID).build());
        promotions.save(Promotion.builder().code("LUMIERE10").name("Chào mừng đến Lumière")
                .type(PromotionType.PERCENT_PRODUCT).scope(PromotionScope.SHOP).shop(shop)
                .value(new BigDecimal("10")).minOrder(new BigDecimal("200000"))
                .maxDiscount(new BigDecimal("50000")).startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(30)).quantity(100).build());
    }
}
