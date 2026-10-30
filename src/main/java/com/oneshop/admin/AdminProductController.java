package com.oneshop.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.common.BusinessException;

import lombok.RequiredArgsConstructor;

/** /admin/products - Admin và Manager đều dùng được (đã cấu hình trong SecurityConfig). */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService productService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) Long shopId,
                       @RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String error, Model model) {
        model.addAttribute("products", productService.search(keyword, shopId, status, page));
        model.addAttribute("shops", productService.shops());
        model.addAttribute("keyword", keyword);
        model.addAttribute("shopId", shopId);
        model.addAttribute("status", status);
        if ("locked".equals(msg)) {
            model.addAttribute("message", "Đã khóa sản phẩm. Sản phẩm bị ẩn khỏi cửa hàng và Vendor không thể hiện lại.");
        } else if ("unlocked".equals(msg)) {
            model.addAttribute("message", "Đã mở khóa sản phẩm, sản phẩm hiển thị trở lại.");
        } else if ("deleted".equals(msg)) {
            model.addAttribute("message", "Đã xóa sản phẩm.");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/products";
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) Long shopId, @RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            productService.lock(id);
            return back(redirect, keyword, shopId, status, page, "locked", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, shopId, status, page, null, e.getMessage());
        }
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(required = false) Long shopId, @RequestParam(defaultValue = "") String status,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            productService.unlock(id);
            return back(redirect, keyword, shopId, status, page, "unlocked", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, shopId, status, page, null, e.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(required = false) Long shopId, @RequestParam(defaultValue = "") String status,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            productService.delete(id);
            return back(redirect, keyword, shopId, status, page, "deleted", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, shopId, status, page, null, e.getMessage());
        }
    }

    private String back(RedirectAttributes redirect, String keyword, Long shopId, String status, int page,
                        String msg, String error) {
        redirect.addAttribute("keyword", keyword);
        if (shopId != null) {
            redirect.addAttribute("shopId", shopId);
        }
        redirect.addAttribute("status", status);
        redirect.addAttribute("page", page);
        if (msg != null) {
            redirect.addAttribute("msg", msg);
        }
        if (error != null) {
            redirect.addAttribute("error", error);
        }
        return "redirect:/admin/products";
    }
}