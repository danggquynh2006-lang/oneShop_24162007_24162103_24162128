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
    private final ProductImageRepository productImages;
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
        seedSkincareProducts(shop);
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

        sample(shop, blush, "Dior Backstage Rosy Glow 001 Pink", "Dior", "Phấn má, màu 001 Pink.", "1350000", 12,
                "/images/products/dior-backstage-rosy-glow-001-pink.jpg");
        sample(shop, blush, "Dior Backstage Rosy Glow 012 Rosewood", "Dior", "Phấn má, màu 012 Rosewood.", "1350000", 10,
                "/images/products/dior-backstage-rosy-glow-012-rosewood.jpg");
        sample(shop, blush, "Chanel Joues Contraste Intense Rose Radiant", "Chanel", "Phấn má, màu Rose Radiant.", "1650000", 8,
                "/images/products/chanel-joues-contraste-intense-rose-radiant.png");
        sample(shop, blush, "Chanel Joues Contraste Intense Rose Ardent", "Chanel", "Phấn má, màu Rose Ardent.", "1650000", 8,
                "/images/products/chanel-joues-contraste-intense-rose-ardent.png");
        sample(shop, blush, "NARS Afterglow Liquid Blush Orgasm", "NARS", "Má hồng dạng lỏng, màu Orgasm.", "1100000", 14,
                "/images/products/nars-afterglow-liquid-blush-orgasm.jpg");
        sample(shop, blush, "NARS Afterglow Liquid Blush Dolce Vita", "NARS", "Má hồng dạng lỏng, màu Dolce Vita.", "1100000", 11,
                "/images/products/nars-afterglow-liquid-blush-dolce-vita.jpg");
        sample(shop, lipstick, "Dior Rouge Dior 999", "Dior", "Son môi, màu 999.", "1250000", 15,
                "/images/products/dior-rouge-dior-999.jpg");
        sample(shop, lipstick, "Dior Rouge Dior 100 Nude Look", "Dior", "Son môi, màu 100 Nude Look.", "1250000", 12,
                "/images/products/dior-rouge-dior-100-nude-look.jpg");
        sample(shop, lipstick, "Dior Rouge Dior 720 Icone", "Dior", "Son môi, màu 720 Icone.", "1250000", 10,
                "/images/products/dior-rouge-dior-720-icone.jpg");
        sample(shop, lipstick, "Chanel Rouge Allure Velvet 58 Rouge Vie", "Chanel", "Son lì, màu 58 Rouge Vie.", "1450000", 9,
                "/images/products/chanel-rouge-allure-velvet-58-rouge-vie.png");
        sample(shop, lipstick, "Gucci Rouge à Lèvres Mat 25 Goldie Red", "Gucci", "Son lì, màu 25 Goldie Red.", "1500000", 7,
                "/images/products/gucci-rouge-a-levres-mat-25-goldie-red.jpg");
        sample(shop, lipstick, "Tom Ford Runway Lip Color 16 Scarlet Rouge", "Tom Ford", "Son môi, màu 16 Scarlet Rouge.", "1650000", 6,
                "/images/products/tom-ford-runway-lip-color-16-scarlet-rouge.jpg");
        sample(shop, eyePalette, "Dior Backstage Eye Palette 001 Warm Neutrals", "Dior", "Bảng mắt 001 Warm Neutrals.", "1750000", 10,
                "/images/products/dior-backstage-eye-palette-001-warm-neutrals.jpg");
        sample(shop, eyePalette, "Dior Backstage Eye Palette 002 Smoky Essentials", "Dior", "Bảng mắt 002 Smoky Essentials.", "1750000", 10,
                "/images/products/dior-backstage-eye-palette-002-smoky-essentials.jpg");
        sample(shop, eyePalette, "Chanel Les 4 Ombres 228 Tissé Cambon", "Chanel", "Bảng mắt bốn màu 228 Tissé Cambon.", "2050000", 8,
                "/images/products/chanel-les-4-ombres-228-tisse-cambon.png");
        sample(shop, eyePalette, "Chanel Les 4 Ombres 308 Clair-Obscur", "Chanel", "Bảng mắt bốn màu 308 Clair-Obscur.", "2050000", 8,
                "/images/products/chanel-les-4-ombres-308-clair-obscur.png");
        sample(shop, eyePalette, "Pat McGrath Mothership VII Divine Rose", "Pat McGrath Labs", "Bảng mắt Mothership VII Divine Rose.", "3350000", 5,
                "/images/products/pat-mcgrath-mothership-vii-divine-rose.jpg");
        sample(shop, eyePalette, "Pat McGrath Mothership X Moonlit Seduction", "Pat McGrath Labs", "Bảng mắt Mothership X Moonlit Seduction.", "3350000", 5,
                "/images/products/pat-mcgrath-mothership-x-moonlit-seduction.jpg");
    }

    private void seedSkincareProducts(Shop shop) {
        Category skincare = categories.findFirstByNameIgnoreCaseAndParentIsNull("Chăm sóc da").orElseThrow();
        Category serum = categories.findFirstByNameIgnoreCaseAndParentId("Serum", skincare.getId()).orElseThrow();
        Category toner = categories.findFirstByNameIgnoreCaseAndParentId("Toner", skincare.getId()).orElseThrow();
        Category moisturizer = categories.findFirstByNameIgnoreCaseAndParentId("Kem dưỡng", skincare.getId()).orElseThrow();

        sample(shop, serum, "Estée Lauder Advanced Night Repair 50ml", "Estée Lauder",
                "Tinh chất phục hồi Advanced Night Repair.", "3850000", 12,
                "/images/products/estee-lauder-advanced-night-repair-50ml.png");
        sample(shop, toner, "SK-II PITERA Facial Treatment Essence 230ml", "SK-II",
                "Nước thần PITERA Facial Treatment Essence.", "5850000", 9,
                "/images/products/sk-ii-pitera-facial-treatment-essence-230ml.png");
        sample(shop, moisturizer, "La Mer Crème de la Mer 60ml", "La Mer",
                "Kem dưỡng ẩm Crème de la Mer.", "9800000", 5,
                "/images/products/la-mer-creme-de-la-mer-60ml.jpg");
        sample(shop, moisturizer, "Kiehl's Ultra Facial Cream 125ml", "Kiehl's",
                "Kem dưỡng ẩm Ultra Facial Cream.", "1750000", 12,
                "/images/products/kiehl-s-ultra-facial-cream-125ml.webp");
        sample(shop, serum, "Shiseido Ultimune Power Infusing Concentrate 50ml", "Shiseido",
                "Tinh chất Ultimune Power Infusing Concentrate.", "3150000", 8,
                "/images/products/shiseido-ultimune-power-infusing-concentrate-50ml.png");
        sample(shop, moisturizer, "Clinique Moisture Surge 100H 50ml", "Clinique",
                "Gel dưỡng ẩm Moisture Surge 100H.", "1250000", 14,
                "/images/products/clinique-moisture-surge-100h-50ml.jpg");
        sample(shop, serum, "Clarins Double Serum 50ml", "Clarins",
                "Tinh chất kép Double Serum.", "3650000", 7,
                "/images/products/clarins-double-serum-50ml.png");
        sample(shop, serum, "Lancôme Advanced Génifique 50ml", "Lancôme",
                "Tinh chất Advanced Génifique Youth Activating Concentrate.", "3200000", 10,
                "/images/products/lancome-advanced-genifique-50ml.jpg");
        sample(shop, serum, "Dior Capture Totale Le Sérum 50ml", "Dior",
                "Tinh chất Capture Totale Le Sérum.", "4900000", 6,
                "/images/products/dior-capture-totale-le-serum-50ml.png");
    }

    private void sample(Shop shop, Category category, String name, String brand, String description,
            String price, int stock, String imageUrl) {
        Product product = products.save(Product.builder().shop(shop).category(category).name(name).brand(brand)
                .description(description).price(new BigDecimal(price)).stock(stock).build());
        productImages.save(ProductImage.builder().product(product).url(imageUrl).mainImage(true).build());
    }
}
