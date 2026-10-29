package com.oneshop.admin;

import org.springframework.dao.DataIntegrityViolationException;
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

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final AdminProductService productService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String error,
                       Model model) {
        model.addAttribute("products", productService.search(keyword, page));
        model.addAttribute("keyword", keyword);
        model.addAttribute("message", msg);
        model.addAttribute("error", error);
        return "admin/products";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        productService.toggle(id);
        return back(redirect, keyword, page, "Đã đổi trạng thái sản phẩm.", null);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            productService.delete(id);
            return back(redirect, keyword, page, "Đã xóa sản phẩm.", null);
        } catch (BusinessException | DataIntegrityViolationException exception) {
            String message = exception instanceof BusinessException ? exception.getMessage()
                    : "Sản phẩm đang được tham chiếu; hãy ẩn sản phẩm để giữ dữ liệu liên quan.";
            return back(redirect, keyword, page, null, message);
        }
    }

    private String back(RedirectAttributes redirect, String keyword, int page, String message, String error) {
        redirect.addAttribute("keyword", keyword);
        redirect.addAttribute("page", page);
        if (message != null) redirect.addAttribute("msg", message);
        if (error != null) redirect.addAttribute("error", error);
        return "redirect:/admin/products";
    }
}
