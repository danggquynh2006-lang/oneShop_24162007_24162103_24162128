package com.oneshop.vendor;

import org.springframework.dao.DataIntegrityViolationException;
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

import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import com.oneshop.entity.Promotion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/vendor/promotions")
@RequiredArgsConstructor
public class VendorPromotionController {

    private final VendorPromotionService promotionService;
    private final ShopService shopService;

    private Long shopId() {
        return shopService.requireActiveShop(SecurityUtil.getCurrentUser()).getId();
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String error,
                       Model model) {
        model.addAttribute("promotions", promotionService.list(shopId(), page));
        String text = switch (msg == null ? "" : msg) {
            case "created" -> "Đã tạo khuyến mãi.";
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
        return "vendor/promotions";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        shopId();
        return showForm(model, new PromotionForm(), "Tạo khuyến mãi", "/vendor/promotions", null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PromotionForm form, BindingResult result,
                         Model model) {
        Long shopId = shopId();
        if (!result.hasErrors()) {
            try {
                promotionService.create(shopId, form);
                return "redirect:/vendor/promotions?msg=created";
            } catch (BusinessException e) {
                model.addAttribute("formError", e.getMessage());
            }
        }
        return showForm(model, form, "Tạo khuyến mãi", "/vendor/promotions", null);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Promotion p = promotionService.get(shopId(), id);
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
        return showForm(model, form, "Sửa khuyến mãi", "/vendor/promotions/" + id, p);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") PromotionForm form, BindingResult result,
                         Model model) {
        Long shopId = shopId();
        if (!result.hasErrors()) {
            try {
                promotionService.update(shopId, id, form);
                return "redirect:/vendor/promotions?msg=updated";
            } catch (BusinessException e) {
                model.addAttribute("formError", e.getMessage());
            }
        }
        return showForm(model, form, "Sửa khuyến mãi", "/vendor/promotions/" + id,
                promotionService.get(shopId, id));
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,
                         RedirectAttributes redirect) {
        promotionService.toggleActive(shopId(), id);
        return back(redirect, page, "toggled", null);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,
                         RedirectAttributes redirect) {
        try {
            promotionService.delete(shopId(), id);
            return back(redirect, page, "deleted", null);
        } catch (BusinessException e) {
            return back(redirect, page, null, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return back(redirect, page, null, "Mã đang được dùng trong đơn hàng nên không thể xóa. Hãy tắt mã.");
        }
    }

    private String showForm(Model model, PromotionForm form, String title, String action, Promotion current) {
        model.addAttribute("form", form);
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("current", current);
        return "vendor/promotion-form";
    }

    private String back(RedirectAttributes redirect, int page, String msg, String error) {
        redirect.addAttribute("page", page);
        if (msg != null) {
            redirect.addAttribute("msg", msg);
        }
        if (error != null) {
            redirect.addAttribute("error", error);
        }
        return "redirect:/vendor/promotions";
    }
}