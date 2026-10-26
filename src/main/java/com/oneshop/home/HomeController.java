package com.oneshop.home;

import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.entity.User;
import com.oneshop.favorite.FavoriteService;
import com.oneshop.favorite.ViewedService;
import com.oneshop.user.ReviewService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeCategoryService categoryService;
    private final HomeProductService productService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final ViewedService viewedService;

    @GetMapping("/")
    public String home(Principal principal,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        var tree = categoryService.activeTree();

        model.addAttribute("categories", tree);
        model.addAttribute("loggedIn", principal != null);
        model.addAttribute("featured", tree.stream().limit(4).toList());
        model.addAttribute("products", productService.bestSellers(page));
        model.addAttribute("suggestions", productService.suggestions());
        return "web/home";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id,
                                @RequestParam(defaultValue = "0") int reviewPage, Model model) {
        var product = productService.detail(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        User user = SecurityUtil.getCurrentUser();
        boolean favorited = false;
        if (user != null) {
            try {
                viewedService.record(user, id);
            } catch (RuntimeException e) {
            }
            favorited = favoriteService.isFavorite(user, id);
        }

        model.addAttribute("id", id);
        model.addAttribute("product", product);
        model.addAttribute("favorited", favorited);
        model.addAttribute("related", productService.related(product));
        model.addAttribute("reviewSection", reviewService.section(id, user, reviewPage));
        return "web/product-detail";
    }
}