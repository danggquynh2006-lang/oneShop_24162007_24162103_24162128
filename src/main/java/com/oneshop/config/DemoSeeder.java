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
                "https://www.dior.com/on/demandware.static/-/Sites-master_dior/default/dwc9998712/Y0000049/Y0000049_E000000573_E01_RHC.jpg?sw=886&sh=1328");
        sample(shop, blush, "Dior Backstage Rosy Glow 012 Rosewood", "Dior", "Phấn má, màu 012 Rosewood.", "1350000", 10,
                "https://www.dior.com/on/demandware.static/-/Sites-master_dior/default/dwc706af14/Y0000049/Y0000049_E000000575_E01_RHC.jpg?sw=886&sh=1328");
        sample(shop, blush, "Chanel Joues Contraste Intense Rose Radiant", "Chanel", "Phấn má, màu Rose Radiant.", "1650000", 8,
                "https://www.chanel.com/images/t_one/w_0.43,h_0.43,c_crop/q_auto:good,f_autoplus,fl_lossy,dpr_1.1/w_840/joues-contraste-intense-cream-to-powder-blush-rose-radiant-0-28oz--packshot-default-168242-9543847936030.jpg");
        sample(shop, blush, "Chanel Joues Contraste Intense Rose Ardent", "Chanel", "Phấn má, màu Rose Ardent.", "1650000", 8,
                "https://www.chanel.com/images/t_one/w_0.43,h_0.43,c_crop/q_auto:good,f_autoplus,fl_lossy,dpr_1.1/w_840/joues-contraste-intense-cream-to-powder-blush-rose-ardent-0-28oz--packshot-default-168238-9543849050142.jpg");
        sample(shop, blush, "NARS Afterglow Liquid Blush Orgasm", "NARS", "Má hồng dạng lỏng, màu Orgasm.", "1100000", 14,
                "https://www.narscosmetics.com/dw/image/v2/BBSK_PRD/on/demandware.static/-/Sites-itemmaster_NARS/default/dw44868227/2023/December/AfterglowBlush/0194251132020_Orgasm_1.jpg?sw=856&sh=750&sm=fit");
        sample(shop, blush, "NARS Afterglow Liquid Blush Dolce Vita", "NARS", "Má hồng dạng lỏng, màu Dolce Vita.", "1100000", 11,
                "https://www.narscosmetics.com/dw/image/v2/BBSK_PRD/on/demandware.static/-/Sites-itemmaster_NARS/default/dw849f9edf/2023/December/AfterglowBlush/0194251132044_DolceVita_1.jpg?sw=856&sh=750&sm=fit");
        sample(shop, lipstick, "Dior Rouge Dior 999", "Dior", "Son môi, màu 999.", "1250000", 15,
                "https://www.dior.com/dw/image/v2/BGXS_PRD/on/demandware.static/-/Sites-master_dior/default/dw98703630/Y0356009/Y0356009_C035600999_E01_RHC.jpg?sw=640");
        sample(shop, lipstick, "Dior Rouge Dior 100 Nude Look", "Dior", "Son môi, màu 100 Nude Look.", "1250000", 12,
                "https://www.dior.com/dw/image/v2/BGXS_PRD/on/demandware.static/-/Sites-master_dior/default/dw26c9e2fa/Y0356009/Y0356009_C035500100_E01_RHC.jpg?sw=640");
        sample(shop, lipstick, "Dior Rouge Dior 720 Icone", "Dior", "Son môi, màu 720 Icone.", "1250000", 10,
                "https://www.dior.com/dw/image/v2/BGXS_PRD/on/demandware.static/-/Sites-master_dior/default/dwd1e829b0/Y0356009/Y0356009_C047000720_E01_RHC.jpg?sw=640");
        sample(shop, lipstick, "Chanel Rouge Allure Velvet 58 Rouge Vie", "Chanel", "Son lì, màu 58 Rouge Vie.", "1450000", 9,
                "https://www.chanel.com/images/t_one/w_0.38,h_0.38,c_crop/q_auto:good,f_autoplus,fl_lossy,dpr_1.1/w_840/rouge-allure-velvet-luminous-matte-lip-colour-58-rouge-vie-0-12oz--packshot-default-162580-9564896559134.jpg");
        sample(shop, lipstick, "Gucci Rouge à Lèvres Mat 25 Goldie Red", "Gucci", "Son lì, màu 25 Goldie Red.", "1500000", 7,
                "https://media.gucci.com/style/DarkGray_Center_0_0_1200x1200/1773939752/611234_9PL12_9250_001_100_0000_Light-25-goldie-red-rouge-a-levres-mat-lipstick.jpg");
        sample(shop, lipstick, "Tom Ford Runway Lip Color 16 Scarlet Rouge", "Tom Ford", "Son môi, màu 16 Scarlet Rouge.", "1650000", 6,
                "https://www.tomfordbeauty.com/cdn/shop/files/tf_sku_T0ZW01_3000x3000_0_2c5f16e9-ed1a-4ed1-bac2-c1779d9eebe0.png?v=1792569749&width=832");
        sample(shop, eyePalette, "Dior Backstage Eye Palette 001 Warm Neutrals", "Dior", "Bảng mắt 001 Warm Neutrals.", "1750000", 10,
                "https://www.dior.com/dw/image/v2/BGXS_PRD/on/demandware.static/-/Sites-master_dior/default/dw0d407933/Y0012000/Y0012000_C038300001_E01_ZHC.jpg?sw=1280");
        sample(shop, eyePalette, "Dior Backstage Eye Palette 002 Smoky Essentials", "Dior", "Bảng mắt 002 Smoky Essentials.", "1750000", 10,
                "https://www.dior.com/dw/image/v2/BGXS_PRD/on/demandware.static/-/Sites-master_dior/default/dwb386a107/Y0012000/Y0012000_C038300002_E01_ZHC.jpg?sw=1280");
        sample(shop, eyePalette, "Chanel Les 4 Ombres 228 Tissé Cambon", "Chanel", "Bảng mắt bốn màu 228 Tissé Cambon.", "2050000", 8,
                "https://www.chanel.com/images/t_one/w_0.43,h_0.43,c_crop/q_auto:good,f_autoplus,fl_lossy,dpr_1.1/w_840/les-4-ombres-multi-effect-quadra-eyeshadow-228-tisse-cambon-0-07oz--packshot-default-164228-9564895707166.jpg");
        sample(shop, eyePalette, "Chanel Les 4 Ombres 308 Clair-Obscur", "Chanel", "Bảng mắt bốn màu 308 Clair-Obscur.", "2050000", 8,
                "https://www.chanel.com/images/t_one/w_0.43,h_0.43,c_crop/q_auto:good,f_autoplus,fl_lossy,dpr_1.1/w_840/les-4-ombres-multi-effect-quadra-eyeshadow-308-clair-obscur-0-07oz--packshot-default-164308-8814057095198.jpg");
        sample(shop, eyePalette, "Pat McGrath Mothership VII Divine Rose", "Pat McGrath Labs", "Bảng mắt Mothership VII Divine Rose.", "3350000", 5,
                "https://www.patmcgrath.com/cdn/shop/products/20200820_PMG_EYE_MS7_COLORUPDATE_1200x1200_fea94766-3282-4136-b286-7e5c363f1b52.jpg?v=1629943395");
        sample(shop, eyePalette, "Pat McGrath Mothership X Moonlit Seduction", "Pat McGrath Labs", "Bảng mắt Mothership X Moonlit Seduction.", "3350000", 5,
                "https://www.patmcgrath.com/cdn/shop/products/PMG_EYE_MSX_COMP_OPEN_1200x1200_ca7ce8df-d2d7-42d5-bf6f-e03090cc8f39.jpg?v=1658425123");
    }

    private void sample(Shop shop, Category category, String name, String brand, String description,
            String price, int stock, String imageUrl) {
        Product product = products.save(Product.builder().shop(shop).category(category).name(name).brand(brand)
                .description(description).price(new BigDecimal(price)).stock(stock).build());
        productImages.save(ProductImage.builder().product(product).url(imageUrl).mainImage(true).build());
    }
}
