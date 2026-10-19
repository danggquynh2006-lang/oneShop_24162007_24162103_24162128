package com.oneshop.auth;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.common.Role;
import com.oneshop.config.JwtAuthenticationFilter;
import com.oneshop.config.JwtService;
import com.oneshop.entity.User;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    // ===== Đăng ký =====

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

    // ===== Xác thực OTP đăng ký =====

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

    // ===== Đăng nhập =====

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String registered,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String reset, Model model) {
        User current = SecurityUtil.getCurrentUser();
        if (current != null) {
            return "redirect:" + homeFor(current.getRole());
        }
        if (registered != null) {
            model.addAttribute("message", "Đăng ký thành công! Hãy đăng nhập.");
        }
        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất.");
        }
        if (reset != null) {
            model.addAttribute("message", "Đặt lại mật khẩu thành công! Hãy đăng nhập bằng mật khẩu mới.");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        Model model, HttpServletResponse response) {
        try {
            User user = authService.login(email, password);
            ResponseCookie cookie = ResponseCookie
                    .from(JwtAuthenticationFilter.COOKIE_NAME, jwtService.generate(user))
                    .httpOnly(true)
                    .secure(false) // chạy http://localhost; khi deploy https thì đổi thành true
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofSeconds(jwtService.getExpirationSeconds()))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return "redirect:" + homeFor(user.getRole());
        } catch (AuthException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "auth/login";
        }
    }

    // ===== Quên mật khẩu =====

    @GetMapping("/forgot-password")
    public String forgotForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgot(@RequestParam String email, Model model, RedirectAttributes redirect) {
        if (email.isBlank()) {
            model.addAttribute("error", "Vui lòng nhập email.");
            return "auth/forgot-password";
        }
        try {
            authService.requestPasswordReset(email);
        } catch (AuthException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "auth/forgot-password";
        }
        redirect.addAttribute("email", email.trim().toLowerCase());
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam String email,
                            @RequestParam(required = false) String resent, Model model) {
        ResetPasswordRequest form = new ResetPasswordRequest();
        form.setEmail(email);
        model.addAttribute("form", form);
        if (resent != null) {
            model.addAttribute("message", "Đã gửi lại mã OTP, vui lòng kiểm tra email.");
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(@Valid @ModelAttribute("form") ResetPasswordRequest form,
                        BindingResult result, Model model) {
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
                && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (result.hasErrors()) {
            return "auth/reset-password";
        }
        try {
            authService.resetPassword(form);
        } catch (AuthException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }
        return "redirect:/login?reset";
    }

    @PostMapping("/reset-password/resend")
    public String resendReset(@RequestParam String email, Model model, RedirectAttributes redirect) {
        try {
            authService.requestPasswordReset(email);
        } catch (AuthException e) {
            ResetPasswordRequest form = new ResetPasswordRequest();
            form.setEmail(email);
            model.addAttribute("form", form);
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }
        redirect.addAttribute("email", email);
        redirect.addAttribute("resent", "1");
        return "redirect:/reset-password";
    }

    // ===== Tiện ích =====

    /** Trang đích sau khi đăng nhập theo vai trò (Vendor sẽ bổ sung khi có trang). */
    private String homeFor(Role role) {
        return switch (role) {
            case ADMIN, MANAGER -> "/admin/users";
            case SHIPPER -> "/shipper/orders";
            default -> "/";
        };
    }
}