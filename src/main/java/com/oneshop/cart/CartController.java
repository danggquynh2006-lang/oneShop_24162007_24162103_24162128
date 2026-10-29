package com.oneshop.cart;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.entity.User;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart")
    public String cart(Model model) {
        User user = SecurityUtil.getCurrentUser();
        model.addAttribute("groups", user == null ? List.of() : cartService.view(user));
        return "web/cart";
    }
}