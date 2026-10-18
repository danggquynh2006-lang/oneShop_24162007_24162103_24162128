package com.oneshop.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeCategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.activeTree());
        return "web/home";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return "web/product-detail";
    }
}