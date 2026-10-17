package com.oneshop.order;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutAddressRepository addressRepo;

    public record Line(String name, long price, int quantity) {}
    public record ShopOrder(String shopName, List<Line> lines) {}
    public record CarrierOption(String code, String name, long fee) {}

    @GetMapping("/user/checkout")
    public String checkout(Principal principal, Model model) {
        List<ShopOrder> shops = List.of(
                new ShopOrder("Shop mẫu 1", List.of(
                        new Line("Sản phẩm mẫu 1", 199000, 1),
                        new Line("Sản phẩm mẫu 2", 250000, 2))),
                new ShopOrder("Shop mẫu 2", List.of(
                        new Line("Sản phẩm mẫu 3", 120000, 1))));

        List<CarrierOption> carriers = List.of(
                new CarrierOption("FAST", "Giao nhanh", 30000),
                new CarrierOption("SAVE", "Giao tiết kiệm", 15000));

        long subtotal = shops.stream()
                .flatMap(s -> s.lines().stream())
                .mapToLong(l -> l.price() * l.quantity())
                .sum();

        BigDecimal shippingFee = BigDecimal.valueOf(carriers.get(0).fee())
                .multiply(BigDecimal.valueOf(shops.size()));

        BigDecimal total = PriceCalculator.total(
                BigDecimal.valueOf(subtotal), BigDecimal.ZERO, BigDecimal.ZERO,
                shippingFee, BigDecimal.ZERO);

        model.addAttribute("addresses", addressRepo.listForUser(principal.getName()));
        model.addAttribute("shops", shops);
        model.addAttribute("carriers", carriers);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shopCount", shops.size());
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);
        return "web/checkout";
    }
}