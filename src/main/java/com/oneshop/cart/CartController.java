package com.oneshop.cart;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    public record CartLine(Long productId, String name, long price, int quantity) {}
    public record ShopGroup(String shopName, List<CartLine> lines) {}

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("groups", List.of(
                new ShopGroup("Shop mẫu 1", List.of(
                        new CartLine(1L, "Sản phẩm mẫu 1", 199000, 1),
                        new CartLine(2L, "Sản phẩm mẫu 2", 250000, 2))),
                new ShopGroup("Shop mẫu 2", List.of(
                        new CartLine(3L, "Sản phẩm mẫu 3", 120000, 1)))));
        return "web/cart";
    }
}