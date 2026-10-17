package com.oneshop.admin;

import java.math.RoundingMode;

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

import com.oneshop.common.BusinessException;
import com.oneshop.entity.Carrier;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/carriers")
@RequiredArgsConstructor
public class AdminCarrierController {

    private final AdminCarrierService carrierService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String error,
                       Model model) {
        model.addAttribute("carriers", carrierService.search(keyword, page));
        model.addAttribute("keyword", keyword);

        if ("created".equals(msg)) {
            model.addAttribute("message", "Đã thêm nhà vận chuyển.");
        } else if ("updated".equals(msg)) {
            model.addAttribute("message", "Đã cập nhật nhà vận chuyển.");
        } else if ("toggled".equals(msg)) {
            model.addAttribute("message", "Đã thay đổi trạng thái hoạt động.");
        } else if ("deleted".equals(msg)) {
            model.addAttribute("message", "Đã xóa nhà vận chuyển.");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/carriers";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        return showForm(model, new CarrierForm(), "Thêm nhà vận chuyển", "/admin/carriers");
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") CarrierForm form, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                carrierService.create(form);
                return "redirect:/admin/carriers?msg=created";
            } catch (BusinessException e) {
                model.addAttribute("formError", e.getMessage());
            }
        }
        return showForm(model, form, "Thêm nhà vận chuyển", "/admin/carriers");
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Carrier carrier = carrierService.get(id);
        CarrierForm form = new CarrierForm();
        form.setName(carrier.getName());
        // DB lưu dạng 30000.00; bỏ phần thập phân để ô nhập hiện 30000
        form.setShippingFee(carrier.getShippingFee().setScale(0, RoundingMode.HALF_UP));
        form.setActive(carrier.isActive());
        return showForm(model, form, "Sửa nhà vận chuyển", "/admin/carriers/" + id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") CarrierForm form,
                         BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                carrierService.update(id, form);
                return "redirect:/admin/carriers?msg=updated";
            } catch (BusinessException e) {
                model.addAttribute("formError", e.getMessage());
            }
        }
        return showForm(model, form, "Sửa nhà vận chuyển", "/admin/carriers/" + id);
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id,
                         @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         RedirectAttributes redirect) {
        try {
            carrierService.toggleActive(id);
            return back(redirect, keyword, page, "toggled", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, page, null, e.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         RedirectAttributes redirect) {
        try {
            carrierService.delete(id);
            return back(redirect, keyword, page, "deleted", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, page, null, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return back(redirect, keyword, page, null,
                    "Nhà vận chuyển đã được dùng trong đơn hàng nên không thể xóa. Hãy bấm Tạm dừng thay vì xóa.");
        }
    }

    private String showForm(Model model, CarrierForm form, String title, String action) {
        model.addAttribute("form", form);
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        return "admin/carrier-form";
    }

    private String back(RedirectAttributes redirect, String keyword, int page, String msg, String error) {
        redirect.addAttribute("keyword", keyword);
        redirect.addAttribute("page", page);
        if (msg != null) {
            redirect.addAttribute("msg", msg);
        }
        if (error != null) {
            redirect.addAttribute("error", error);
        }
        return "redirect:/admin/carriers";
    }
}