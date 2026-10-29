package com.oneshop.home;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Product;
import com.oneshop.repository.CategoryRepository;
import com.oneshop.repository.ProductRepository;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.entity.User;
import com.oneshop.user.ReviewService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeCategoryService categoryService;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final ProductImageService productImages;
    private final ReviewService reviewService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.activeTree());
        List<Product> featured = products.findFeatured(ShopStatus.ACTIVE, PageRequest.of(0, 8));
        model.addAttribute("products", featured);
        model.addAttribute("imageUrls", productImages.mainUrls(featured));
        return "web/home";
    }

    @GetMapping("/category/{id}")
    public String category(@PathVariable Long id, Model model) {
        model.addAttribute("category", categories.findById(id).filter(category -> category.isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục.")));
        List<Product> catalog = products.findPublicByCategory(id, ShopStatus.ACTIVE);
        model.addAttribute("products", catalog);
        model.addAttribute("imageUrls", productImages.mainUrls(catalog));
        return "web/category";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = products.findPublicById(id, ShopStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm."));
        model.addAttribute("product", product);
        model.addAttribute("imageUrl", productImages.mainUrls(List.of(product)).get(product.getId()));
        model.addAttribute("reviews", reviewService.list(product.getId()));
        User currentUser = SecurityUtil.getCurrentUser();
        model.addAttribute("canReview", reviewService.canReview(product.getId(),
                currentUser == null ? null : currentUser.getId()));
        model.addAttribute("reviewForm", new com.oneshop.user.ReviewForm());
        return "web/product-detail";
    }
}
