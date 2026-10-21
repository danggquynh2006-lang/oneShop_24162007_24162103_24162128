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
        Category category = categories.findFirstByNameIgnoreCaseAndParentIsNull("Chăm sóc da").orElseThrow();
        Shop shop = shops.save(Shop.builder().owner(owner).name("Lumière Beauty")
                .description("Mỹ phẩm chăm sóc da và trang điểm chọn lọc cho mỗi ngày.")
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
        seedHighEndMakeup(shop);
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

    private void seedHighEndMakeup(Shop shop) {
        Category makeup = categories.findFirstByNameIgnoreCaseAndParentIsNull("Trang điểm").orElseThrow();
        Category blush = categories.findFirstByNameIgnoreCaseAndParentId("Má hồng", makeup.getId()).orElseThrow();
        Category lipstick = categories.findFirstByNameIgnoreCaseAndParentId("Son môi", makeup.getId()).orElseThrow();
        Category eyePalette = categories.findFirstByNameIgnoreCaseAndParentId("Bảng mắt", makeup.getId()).orElseThrow();

        products.saveAll(List.of(
                sample(shop, blush, "Dior Backstage Rosy Glow 001 Pink", "Dior", "Phấn má, màu 001 Pink.", "1350000", 12),
                sample(shop, blush, "Dior Backstage Rosy Glow 012 Rosewood", "Dior", "Phấn má, màu 012 Rosewood.", "1350000", 10),
                sample(shop, blush, "Chanel Joues Contraste Intense Rose Radiant", "Chanel", "Phấn má, màu Rose Radiant.", "1650000", 8),
                sample(shop, blush, "Chanel Joues Contraste Intense Rose Ardent", "Chanel", "Phấn má, màu Rose Ardent.", "1650000", 8),
                sample(shop, blush, "NARS Afterglow Liquid Blush Orgasm", "NARS", "Má hồng dạng lỏng, màu Orgasm.", "1100000", 14),
                sample(shop, blush, "NARS Afterglow Liquid Blush Dolce Vita", "NARS", "Má hồng dạng lỏng, màu Dolce Vita.", "1100000", 11),
                sample(shop, lipstick, "Dior Rouge Dior 999", "Dior", "Son môi, màu 999.", "1250000", 15),
                sample(shop, lipstick, "Dior Rouge Dior 100 Nude Look", "Dior", "Son môi, màu 100 Nude Look.", "1250000", 12),
                sample(shop, lipstick, "Dior Rouge Dior 720 Icone", "Dior", "Son môi, màu 720 Icone.", "1250000", 10),
                sample(shop, lipstick, "Chanel Rouge Allure Velvet 58 Rouge Vie", "Chanel", "Son lì, màu 58 Rouge Vie.", "1450000", 9),
                sample(shop, lipstick, "Gucci Rouge à Lèvres Mat 25 Goldie Red", "Gucci", "Son lì, màu 25 Goldie Red.", "1500000", 7),
                sample(shop, lipstick, "Tom Ford Runway Lip Color 16 Scarlet Rouge", "Tom Ford", "Son môi, màu 16 Scarlet Rouge.", "1650000", 6),
                sample(shop, eyePalette, "Dior Backstage Eye Palette 001 Warm Neutrals", "Dior", "Bảng mắt 001 Warm Neutrals.", "1750000", 10),
                sample(shop, eyePalette, "Dior Backstage Eye Palette 002 Smoky Essentials", "Dior", "Bảng mắt 002 Smoky Essentials.", "1750000", 10),
                sample(shop, eyePalette, "Chanel Les 4 Ombres 228 Tissé Cambon", "Chanel", "Bảng mắt bốn màu 228 Tissé Cambon.", "2050000", 8),
                sample(shop, eyePalette, "Chanel Les 4 Ombres 308 Clair-Obscur", "Chanel", "Bảng mắt bốn màu 308 Clair-Obscur.", "2050000", 8),
                sample(shop, eyePalette, "Pat McGrath Mothership VII Divine Rose", "Pat McGrath Labs", "Bảng mắt Mothership VII Divine Rose.", "3350000", 5),
                sample(shop, eyePalette, "Pat McGrath Mothership X Moonlit Seduction", "Pat McGrath Labs", "Bảng mắt Mothership X Moonlit Seduction.", "3350000", 5)));
    }

    private Product sample(Shop shop, Category category, String name, String brand, String description,
            String price, int stock) {
        return Product.builder().shop(shop).category(category).name(name).brand(brand)
                .description(description).price(new BigDecimal(price)).stock(stock).build();
    }
}
