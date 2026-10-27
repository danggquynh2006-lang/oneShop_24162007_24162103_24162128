package com.oneshop.vendor;

import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import com.oneshop.entity.Product;
import com.oneshop.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller @RequestMapping("/vendor/products") @RequiredArgsConstructor
public class VendorProductController {
    private final VendorProductService productService;
    private final ShopService shopService;
    private final CategoryRepository categoryRepository;

    private Long shopId() { return shopService.requireActiveShop(SecurityUtil.getCurrentUser()).getId(); }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String query,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String error, Model model) {
        model.addAttribute("products", productService.list(shopId(), query, page));
        model.addAttribute("query", query);
        model.addAttribute("error", error);
        return "vendor/products";
    }

    @GetMapping("/new")
    public String createForm(Model model) { return form(model, new ProductForm(), null); }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ProductForm productForm, BindingResult result,
                         @RequestParam(required = false) MultipartFile[] images, Model model) throws IOException {
        if (result.hasErrors()) return form(model, productForm, null);
        try {
            Product product = productService.create(shopId(), productForm, images);
            return "redirect:/vendor/products/" + product.getId() + "/edit";
        } catch (BusinessException exception) {
            model.addAttribute("error", exception.getMessage());
            return form(model, productForm, null);
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.get(shopId(), id);
        ProductForm productForm = new ProductForm();
        productForm.setName(product.getName());
        productForm.setDescription(product.getDescription());
        productForm.setPrice(product.getPrice());
        productForm.setDiscountPercent(product.getDiscountPercent());
        productForm.setStock(product.getStock());
        productForm.setCategoryId(product.getCategory().getId());
        productForm.setBrand(product.getBrand());
        productForm.setActive(product.isActive());
        return form(model, productForm, product);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ProductForm productForm,
                         BindingResult result, @RequestParam(required = false) MultipartFile[] images,
                         Model model) throws IOException {
        if (result.hasErrors()) return form(model, productForm, productService.get(shopId(), id));
        try {
            productService.update(shopId(), id, productForm, images);
            return "redirect:/vendor/products/" + id + "/edit?updated";
        } catch (BusinessException exception) {
            model.addAttribute("error", exception.getMessage());
            return form(model, productForm, productService.get(shopId(), id));
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        productService.toggle(shopId(), id);
        return "redirect:/vendor/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            productService.delete(shopId(), id);
        } catch (BusinessException exception) {
            redirect.addAttribute("error", exception.getMessage());
        }
        return "redirect:/vendor/products";
    }

    @PostMapping("/{id}/images/{imageId}/delete")
    public String removeImage(@PathVariable Long id, @PathVariable Long imageId) {
        productService.removeImage(shopId(), id, imageId);
        return "redirect:/vendor/products/" + id + "/edit";
    }

    private String form(Model model, ProductForm productForm, Product product) {
        model.addAttribute("form", productForm);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        if (product != null) model.addAttribute("images", productService.images(product.getId()));
        return "vendor/product-form";
    }
}
