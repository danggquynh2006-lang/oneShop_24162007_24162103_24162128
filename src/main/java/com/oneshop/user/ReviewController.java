package com.oneshop.user;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** POST /user/reviews/{productId} - phải đăng nhập (đã cấu hình /user/** trong SecurityConfig). */
@Controller
@RequestMapping("/user/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{productId}")
    public String create(@PathVariable Long productId,
                         @Valid @ModelAttribute("reviewForm") ReviewForm form,
                         BindingResult result, RedirectAttributes redirect) {
        String back = "redirect:/product/" + productId + "#reviews";
        if (result.hasErrors()) {
            redirect.addFlashAttribute("reviewError", result.getAllErrors().get(0).getDefaultMessage());
            return back;
        }
        try {
            reviewService.create(SecurityUtil.getCurrentUser(), productId, form);
            redirect.addFlashAttribute("reviewMessage", "Cảm ơn bạn! Đánh giá của bạn đã được đăng.");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("reviewError", e.getMessage());
        }
        return back;
    }
}