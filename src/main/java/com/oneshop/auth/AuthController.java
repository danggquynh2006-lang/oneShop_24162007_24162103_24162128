package com.oneshop.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterRequest form,
                           BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
                && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(form);
        } catch (AuthException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
        redirect.addAttribute("email", form.getEmail().trim().toLowerCase());
        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyForm(@RequestParam String email,
                             @RequestParam(required = false) String resent, Model model) {
        model.addAttribute("email", email);
        if (resent != null) {
            model.addAttribute("message", "Đã gửi lại mã OTP, vui lòng kiểm tra email.");
        }
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verify(@RequestParam String email, @RequestParam String code, Model model) {
        try {
            authService.verifyRegister(email, code);
        } catch (AuthException e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "auth/verify-otp";
        }
        return "redirect:/login?registered";
    }

    @PostMapping("/verify-otp/resend")
    public String resend(@RequestParam String email, Model model, RedirectAttributes redirect) {
        try {
            authService.resendRegisterOtp(email);
        } catch (AuthException e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "auth/verify-otp";
        }
        redirect.addAttribute("email", email);
        redirect.addAttribute("resent", "1");
        return "redirect:/verify-otp";
    }
}