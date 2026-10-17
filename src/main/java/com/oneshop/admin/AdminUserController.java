package com.oneshop.admin;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import com.oneshop.common.Role;
import com.oneshop.entity.User;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService userService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) Role role,
                       @RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String error,
                       Model model) {
        User current = SecurityUtil.getCurrentUser();
        Page<User> users = userService.search(keyword, role, status, page);

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("roleFilter", role == null ? "" : role.name());
        model.addAttribute("status", status);
        model.addAttribute("roles", Role.values());
        model.addAttribute("assignableRoles", userService.assignableRoles(current));
        model.addAttribute("currentUserId", current.getId());
        model.addAttribute("currentRole", current.getRole().name());

        if ("locked".equals(msg)) {
            model.addAttribute("message", "Đã khóa tài khoản.");
        } else if ("unlocked".equals(msg)) {
            model.addAttribute("message", "Đã mở khóa tài khoản.");
        } else if ("role".equals(msg)) {
            model.addAttribute("message", "Đã đổi vai trò.");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/users";
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Long id,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(name = "role", defaultValue = "") String roleFilter,
                       @RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "0") int page,
                       RedirectAttributes redirect) {
        try {
            userService.lock(SecurityUtil.getCurrentUser(), id);
            return back(redirect, keyword, roleFilter, status, page, "locked", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, roleFilter, status, page, null, e.getMessage());
        }
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id,
                         @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(name = "role", defaultValue = "") String roleFilter,
                         @RequestParam(defaultValue = "") String status,
                         @RequestParam(defaultValue = "0") int page,
                         RedirectAttributes redirect) {
        try {
            userService.unlock(SecurityUtil.getCurrentUser(), id);
            return back(redirect, keyword, roleFilter, status, page, "unlocked", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, roleFilter, status, page, null, e.getMessage());
        }
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role newRole,
                             @RequestParam(defaultValue = "") String keyword,
                             @RequestParam(name = "role", defaultValue = "") String roleFilter,
                             @RequestParam(defaultValue = "") String status,
                             @RequestParam(defaultValue = "0") int page,
                             RedirectAttributes redirect) {
        try {
            userService.changeRole(SecurityUtil.getCurrentUser(), id, newRole);
            return back(redirect, keyword, roleFilter, status, page, "role", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, roleFilter, status, page, null, e.getMessage());
        }
    }

    /** Quay lại danh sách, giữ nguyên bộ lọc và trang đang xem. */
    private String back(RedirectAttributes redirect, String keyword, String roleFilter,
                        String status, int page, String msg, String error) {
        redirect.addAttribute("keyword", keyword);
        redirect.addAttribute("role", roleFilter);
        redirect.addAttribute("status", status);
        redirect.addAttribute("page", page);
        if (msg != null) {
            redirect.addAttribute("msg", msg);
        }
        if (error != null) {
            redirect.addAttribute("error", error);
        }
        return "redirect:/admin/users";
    }
}