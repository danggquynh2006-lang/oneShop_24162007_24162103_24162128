package com.oneshop.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.oneshop.auth.SecurityUtil;
import com.oneshop.vendor.ShopService;

import lombok.RequiredArgsConstructor;

/** Vendor xem đánh giá của các sản phẩm trong shop mình (chỉ đọc). */
@Controller
@RequestMapping("/vendor/reviews")
@RequiredArgsConstructor
public class VendorReviewController {

    private final ReviewService reviewService;
    private final ShopService shopService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Long shopId = shopService.requireActiveShop(SecurityUtil.getCurrentUser()).getId();
        model.addAttribute("reviews", reviewService.forShop(shopId, page));
        return "vendor/reviews";
    }
}