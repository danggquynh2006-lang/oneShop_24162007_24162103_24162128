package com.oneshop.vendor;

import java.io.IOException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Shop;
import com.oneshop.home.ProductImageService;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ShopRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller @RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ProductImageService productImageService;

    @GetMapping("/shop/register")
    public String registration(Model model) {
        Shop existing = shopService.findOwnShop(SecurityUtil.getCurrentUser());
        if (existing != null) {
            model.addAttribute("shop", existing);
            return "vendor/shop-pending";
        }
        model.addAttribute("form", new ShopForm());
        return "vendor/shop-register";
    }

    @PostMapping("/shop/register")
    public String register(@Valid @ModelAttribute("form") ShopForm form, BindingResult result,
                           @RequestParam(required = false) MultipartFile logo, Model model) throws IOException {
        if (result.hasErrors()) return "vendor/shop-register";
        try {
            shopService.register(SecurityUtil.getCurrentUser(), form, logo);
            return "redirect:/shop/register";
        } catch (BusinessException exception) {
            model.addAttribute("error", exception.getMessage());
            return "vendor/shop-register";
        }
    }

    @GetMapping("/vendor/shop")
    public String shop(Model model) {
        Shop shop = shopService.requireActiveShop(SecurityUtil.getCurrentUser());
        ShopForm form = new ShopForm();
        form.setName(shop.getName());
        form.setDescription(shop.getDescription());
        form.setAddress(shop.getAddress());
        model.addAttribute("shop", shop);
        model.addAttribute("form", form);
        return "vendor/shop";
    }

    @PostMapping("/vendor/shop")
    public String update(@Valid @ModelAttribute("form") ShopForm form, BindingResult result,
                         @RequestParam(required = false) MultipartFile logo,
                         @RequestParam(required = false) MultipartFile banner, Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("shop", shopService.requireActiveShop(SecurityUtil.getCurrentUser()));
            return "vendor/shop";
        }
        shopService.update(SecurityUtil.getCurrentUser(), form, logo, banner);
        return "redirect:/vendor/shop?updated";
    }

    @GetMapping("/shops/{id}")
    public String publicShop(@PathVariable Long id, Model model) {
        Shop shop = shopRepository.findByIdAndStatus(id, ShopStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy shop."));
        model.addAttribute("shop", shop);
        var catalog = productRepository.findByShopIdAndActiveTrue(id, PageRequest.of(0, 24));
        model.addAttribute("products", catalog);
        model.addAttribute("imageUrls", productImageService.mainUrls(catalog.getContent()));
        return "web/shop";
    }
}
