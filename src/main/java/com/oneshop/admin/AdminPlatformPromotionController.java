package com.oneshop.admin;

import java.time.LocalDateTime;

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
import com.oneshop.entity.Promotion;
import com.oneshop.vendor.PromotionForm;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class AdminPlatformPromotionController {
    private final AdminPlatformPromotionService promotionService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String message,
                       @RequestParam(required = false) String error,
                       Model model) {
        model.addAttribute("promotions", promotionService.list(page));
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        return "admin/promotions";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        PromotionForm form = new PromotionForm();
        form.setStartAt(LocalDateTime.now().withSecond(0).withNano(0));
        form.setEndAt(LocalDateTime.now().plusDays(30).withSecond(0).withNano(0));
        return showForm(model, form, "Tạo khuyến mãi toàn sàn", "/admin/promotions", null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PromotionForm form, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                promotionService.create(form);
                return "redirect:/admin/promotions?message=Đã tạo khuyến mãi toàn sàn.";
            } catch (BusinessException exception) {
                model.addAttribute("formError", exception.getMessage());
            }
        }
        return showForm(model, form, "Tạo khuyến mãi toàn sàn", "/admin/promotions", null);
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Promotion promotion = promotionService.get(id);
        return showForm(model, toForm(promotion), "Sửa khuyến mãi toàn sàn", "/admin/promotions/" + id, promotion);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") PromotionForm form,
                         BindingResult result, Model model) {
        Promotion current = promotionService.get(id);
        if (!result.hasErrors()) {
            try {
                promotionService.update(id, form);
                return "redirect:/admin/promotions?message=Đã cập nhật khuyến mãi toàn sàn.";
            } catch (BusinessException exception) {
                model.addAttribute("formError", exception.getMessage());
            }
        }
        return showForm(model, form, "Sửa khuyến mãi toàn sàn", "/admin/promotions/" + id, current);
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        promotionService.toggle(id);
        return "redirect:/admin/promotions?message=Đã đổi trạng thái khuyến mãi.";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            promotionService.delete(id);
            redirect.addAttribute("message", "Đã xóa khuyến mãi toàn sàn.");
        } catch (BusinessException exception) {
            redirect.addAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    private PromotionForm toForm(Promotion promotion) {
        PromotionForm form = new PromotionForm();
        form.setCode(promotion.getCode());
        form.setName(promotion.getName());
        form.setType(promotion.getType());
        form.setValue(promotion.getValue());
        form.setMinOrder(promotion.getMinOrder());
        form.setMaxDiscount(promotion.getMaxDiscount());
        form.setStartAt(promotion.getStartAt());
        form.setEndAt(promotion.getEndAt());
        form.setQuantity(promotion.getQuantity());
        form.setActive(promotion.isActive());
        return form;
    }

    private String showForm(Model model, PromotionForm form, String title, String action, Promotion current) {
        model.addAttribute("form", form);
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("current", current);
        return "admin/promotion-form";
    }
}
