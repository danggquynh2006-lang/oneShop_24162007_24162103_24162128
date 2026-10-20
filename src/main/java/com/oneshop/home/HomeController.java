package com.oneshop.home;

import java.security.Principal;
import java.util.List;
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

    public record Suggestion(Long id, String name, long price) {}

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        var tree = categoryService.activeTree();

        model.addAttribute("categories", tree);
        model.addAttribute("loggedIn", principal != null);
        model.addAttribute("featured", tree.stream().limit(4).toList());

        model.addAttribute("suggestions", List.of(
                new Suggestion(11L, "Sữa rửa mặt dịu nhẹ", 95000),
                new Suggestion(12L, "Kem dưỡng ẩm ban đêm", 320000),
                new Suggestion(13L, "Son môi lì", 180000),
                new Suggestion(14L, "Dầu gội thảo mộc", 120000)));
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