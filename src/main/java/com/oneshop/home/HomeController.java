package com.oneshop.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.oneshop.auth.SecurityUtil;
import com.oneshop.user.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeCategoryService categoryService;
    private final ReviewService reviewService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.activeTree());
        return "web/home";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id,
                                @RequestParam(defaultValue = "0") int reviewPage, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("reviewSection",
                reviewService.section(id, SecurityUtil.getCurrentUser(), reviewPage));
        return "web/product-detail";
    }
}