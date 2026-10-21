package com.oneshop.home;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.common.ShopStatus;
import com.oneshop.repository.CategoryRepository;
import com.oneshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeCategoryService categoryService;
    private final CategoryRepository categories;
    private final ProductRepository products;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.activeTree());
        model.addAttribute("products", products.findFeatured(ShopStatus.ACTIVE, PageRequest.of(0, 8)));
        return "web/home";
    }

    @GetMapping("/category/{id}")
    public String category(@PathVariable Long id, Model model) {
        model.addAttribute("category", categories.findById(id).filter(category -> category.isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục.")));
        model.addAttribute("products", products.findPublicByCategory(id, ShopStatus.ACTIVE));
        return "web/category";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", products.findPublicById(id, ShopStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm.")));
        return "web/product-detail";
    }
}
