package com.oneshop.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.common.BusinessException;
import com.oneshop.common.PromotionScope;
import com.oneshop.entity.Promotion;
import com.oneshop.vendor.PromotionForm;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** /admin/promotions - Admin và Manager đều dùng được (đã cấu hình trong SecurityConfig). */
@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final AdminPromotionService promotionService;

    @GetMapping
    public String list(@RequestParam(required = false) PromotionScope scope,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String error, Model model) {
        model.addAttribute("promotions", promotionService.list(scope, page));
        model.addAttribute("scope", scope);
        String text = switch (msg == null ? "" : msg) {
            case "created" -> "Đã tạo khuyến mãi toàn sàn.";
            case "updated" -> "Đã cập nhật khuyến mãi.";
            case "toggled" -> "Đã thay đổi trạng thái khuyến mãi.";
            case "deleted" -> "Đã xóa khuyến mãi.";
            default -> null;
        };
        if (text != null) {
            model.addAttribute("message", text);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/promotions";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        return showForm(model, new PromotionForm(), "Tạo khuyến mãi toàn sàn", "/admin/promotions", null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PromotionForm form, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                promotionService.create(form);
                return "redirect:/admin/promotions?msg=created";
            } catch (BusinessException e) {
                model.addAttribute("formError", e.getMessage());
            }
        }
        return showForm(model, form, "Tạo khuyến mãi toàn sàn", "/admin/promotions", null);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, RedirectAttributes redirect, Model model) {
        try {
            Promotion p = promotionService.getPlatform(id);
            PromotionForm form = new PromotionForm();
            form.setCode(p.getCode());
            form.setName(p.getName());
            form.setType(p.getType());
            form.setValue(p.getValue());
            form.setMinOrder(p.getMinOrder());
            form.setMaxDiscount(p.getMaxDiscount());
            form.setStartAt(p.getStartAt());
            form.setEndAt(p.getEndAt());
            form.setQuantity(p.getQuantity());
            form.setActive(p.isActive());
            return showForm(model, form, "Sửa khuyến mãi toàn sàn", "/admin/promotions/" + id, p);
        } catch (BusinessException e) {
            redirect.addAttribute("error", e.getMessage());
            return "redirect:/admin/promotions";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") PromotionForm form,
                         BindingResult result, RedirectAttributes redirect, Model model) {
        try {
            if (!result.hasErrors()) {
                try {
                    promotionService.update(id, form);
                    return "redirect:/admin/promotions?msg=updated";
                } catch (BusinessException e) {
                    model.addAttribute("formError", e.getMessage());
                }
            }
            return showForm(model, form, "Sửa khuyến mãi toàn sàn", "/admin/promotions/" + id,
                    promotionService.getPlatform(id));
        } catch (BusinessException e) {
            redirect.addAttribute("error", e.getMessage());
            return "redirect:/admin/promotions";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam(required = false) PromotionScope scope,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        promotionService.toggleActive(id);
        return back(redirect, scope, page, "toggled", null);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(required = false) PromotionScope scope,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            promotionService.delete(id);
            return back(redirect, scope, page, "deleted", null);
        } catch (BusinessException e) {
            return back(redirect, scope, page, null, e.getMessage());
        }
    }

    private String showForm(Model model, PromotionForm form, String title, String action, Promotion current) {
        model.addAttribute("form", form);
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("current", current);
        return "admin/promotion-form";
    }

    private String back(RedirectAttributes redirect, PromotionScope scope, int page, String msg, String error) {
        if (scope != null) {
            redirect.addAttribute("scope", scope);
        }
        redirect.addAttribute("page", page);
        if (msg != null) {
            redirect.addAttribute("msg", msg);
        }
        if (error != null) {
            redirect.addAttribute("error", error);
        }
        return "redirect:/admin/promotions";
    }
}