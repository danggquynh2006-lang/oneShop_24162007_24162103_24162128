package com.oneshop.admin;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.common.BusinessException;
import com.oneshop.common.ShopStatus;

import lombok.RequiredArgsConstructor;

/** /admin/shops - Admin và Manager đều dùng được (đã cấu hình trong SecurityConfig). */
@Controller
@RequestMapping("/admin/shops")
@RequiredArgsConstructor
public class AdminShopController {

    private final AdminShopService shopService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) ShopStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String error,
                       Model model) {
        model.addAttribute("shops", shopService.search(keyword, status, page));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ShopStatus.values());
        model.addAttribute("pendingCount", shopService.countPending());

        if ("approved".equals(msg)) {
            model.addAttribute("message", "Đã duyệt shop. Chủ shop đã trở thành Vendor.");
        } else if ("rejected".equals(msg)) {
            model.addAttribute("message", "Đã từ chối đăng ký shop.");
        } else if ("locked".equals(msg)) {
            model.addAttribute("message", "Đã khóa shop.");
        } else if ("unlocked".equals(msg)) {
            model.addAttribute("message", "Đã mở khóa shop.");
        } else if ("commission".equals(msg)) {
            model.addAttribute("message", "Đã cập nhật % chiết khấu.");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/shops";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                          @RequestParam(required = false) ShopStatus status,
                          @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            shopService.approve(id);
            return back(redirect, keyword, status, page, "approved", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, status, page, null, e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(required = false) ShopStatus status,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            shopService.reject(id);
            return back(redirect, keyword, status, page, "rejected", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, status, page, null, e.getMessage());
        }
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) ShopStatus status,
                       @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            shopService.lock(id);
            return back(redirect, keyword, status, page, "locked", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, status, page, null, e.getMessage());
        }
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(required = false) ShopStatus status,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            shopService.unlock(id);
            return back(redirect, keyword, status, page, "unlocked", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, status, page, null, e.getMessage());
        }
    }

    @PostMapping("/{id}/commission")
    public String commission(@PathVariable Long id, @RequestParam(required = false) BigDecimal rate,
                             @RequestParam(defaultValue = "") String keyword,
                             @RequestParam(required = false) ShopStatus status,
                             @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            shopService.updateCommission(id, rate);
            return back(redirect, keyword, status, page, "commission", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, status, page, null, e.getMessage());
        }
    }

    private String back(RedirectAttributes redirect, String keyword, ShopStatus status, int page,
                        String msg, String error) {
        redirect.addAttribute("keyword", keyword);
        if (status != null) {
            redirect.addAttribute("status", status);
        }
        redirect.addAttribute("page", page);
        if (msg != null) {
            redirect.addAttribute("msg", msg);
        }
        if (error != null) {
            redirect.addAttribute("error", error);
        }
        return "redirect:/admin/shops";
    }
}