package com.oneshop.admin;

import org.springframework.security.access.prepost.PreAuthorize;
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
import com.oneshop.common.OrderStatus;
import com.oneshop.common.Role;
import com.oneshop.entity.User;

import lombok.RequiredArgsConstructor;

/** /admin/orders - Admin và Manager đều xem được. Chỉ Manager được phân công shipper. */
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService orderService;

    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus status,
                       @RequestParam(defaultValue = "false") boolean unassigned,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String error, Model model) {
        User current = SecurityUtil.getCurrentUser();

        model.addAttribute("orders", orderService.search(status, unassigned, page));
        model.addAttribute("shippers", orderService.shippers());
        model.addAttribute("workload", orderService.shipperWorkload());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("unassigned", unassigned);
        model.addAttribute("canAssign", current != null && current.getRole() == Role.MANAGER);

        if ("assigned".equals(msg)) {
            model.addAttribute("message", "Đã phân công shipper cho đơn hàng.");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/orders";
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id, @RequestParam Long shipperId,
                         @RequestParam(required = false) OrderStatus status,
                         @RequestParam(defaultValue = "false") boolean unassigned,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        try {
            orderService.assignShipper(id, shipperId);
            redirect.addAttribute("msg", "assigned");
        } catch (BusinessException e) {
            redirect.addAttribute("error", e.getMessage());
        }
        if (status != null) {
            redirect.addAttribute("status", status);
        }
        if (unassigned) {
            redirect.addAttribute("unassigned", true);
        }
        redirect.addAttribute("page", page);
        return "redirect:/admin/orders";
    }
}