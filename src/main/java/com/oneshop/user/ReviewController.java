package com.oneshop.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/product/{productId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public String create(@PathVariable Long productId, @Valid @ModelAttribute("reviewForm") ReviewForm form,
                         BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("reviewError", "Vui lòng chọn số sao từ 1 đến 5 và nhập nhận xét tối đa 2000 ký tự.");
            return "redirect:/product/" + productId;
        }
        try {
            reviewService.create(productId, SecurityUtil.getCurrentUser().getId(), form);
            redirect.addFlashAttribute("reviewMessage", "Cảm ơn bạn đã đánh giá sản phẩm.");
        } catch (BusinessException exception) {
            redirect.addFlashAttribute("reviewError", exception.getMessage());
        }
        return "redirect:/product/" + productId;
    }
}
