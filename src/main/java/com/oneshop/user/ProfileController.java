package com.oneshop.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import com.oneshop.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public String profile(@RequestParam(required = false) String msg, Model model) {
        prepare(model, SecurityUtil.getCurrentUser());
        if ("saved".equals(msg)) {
            model.addAttribute("message", "Đã cập nhật thông tin.");
        } else if ("pw".equals(msg)) {
            model.addAttribute("message", "Đã đổi mật khẩu.");
        }
        return "user/profile";
    }

    @PostMapping
    public String update(@Valid @ModelAttribute("profileForm") ProfileForm form, BindingResult result,
                         @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                         Model model) {
        User current = SecurityUtil.getCurrentUser();
        if (!result.hasErrors()) {
            try {
                profileService.updateProfile(current.getId(), form, avatarFile);
                return "redirect:/user/profile?msg=saved";
            } catch (BusinessException e) {
                model.addAttribute("profileError", e.getMessage());
            }
        }
        prepare(model, current);
        return "user/profile";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordForm form,
                                 BindingResult result, Model model) {
        User current = SecurityUtil.getCurrentUser();
        if (!result.hasFieldErrors("newPassword") && !result.hasFieldErrors("confirmPassword")
                && !form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (!result.hasErrors()) {
            try {
                profileService.changePassword(current.getId(), form);
                return "redirect:/user/profile?msg=pw";
            } catch (BusinessException e) {
                model.addAttribute("passwordError", e.getMessage());
            }
        }
        prepare(model, current);
        return "user/profile";
    }

    /** Chuẩn bị dữ liệu cho trang; form nào chưa có trong model thì tạo mới. */
    private void prepare(Model model, User current) {
        model.addAttribute("user", current);
        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setFullName(current.getFullName());
            form.setPhone(current.getPhone());
            model.addAttribute("profileForm", form);
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new ChangePasswordForm());
        }
    }
}