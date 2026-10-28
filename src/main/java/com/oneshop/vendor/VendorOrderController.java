package com.oneshop.vendor;

import org.springframework.stereotype.Controller;
import com.oneshop.entity.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import com.oneshop.common.OrderStatus;
import com.oneshop.payment.VnpayRefundService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller @RequestMapping("/vendor/orders") @RequiredArgsConstructor
public class VendorOrderController {
    private final VendorOrderService orderService;
    private final ShopService shopService;
    private final VnpayRefundService refundService;
    private Long shopId() { return shopService.requireActiveShop(SecurityUtil.getCurrentUser()).getId(); }

    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus status,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("orders", orderService.list(shopId(), status, page));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selected", status);
        return "vendor/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(required = false) String error, Model model) {
        model.addAttribute("order", orderService.get(shopId(), id));
        model.addAttribute("items", orderService.items(id));
        model.addAttribute("payment", orderService.payment(id));
        model.addAttribute("error", error);
        return "vendor/order-detail";
    }
    @GetMapping("/{id}/slip")
    public String slip(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Order order = orderService.get(shopId(), id);
        if (order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.CANCELLED) {
            redirect.addAttribute("error", "Chỉ in phiếu giao cho đơn đã xác nhận.");
            return "redirect:/vendor/orders/" + id;
        }
        model.addAttribute("order", order);
        model.addAttribute("items", orderService.items(id));
        model.addAttribute("payment", orderService.payment(id));
        return "vendor/order-slip";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes redirect) {
        try { orderService.confirm(shopId(), id); }
        catch (BusinessException exception) { redirect.addAttribute("error", exception.getMessage()); }
        return "redirect:/vendor/orders/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirect) {
        try { orderService.reject(shopId(), id); }
        catch (BusinessException exception) { redirect.addAttribute("error", exception.getMessage()); }
        return "redirect:/vendor/orders/" + id;
    }

    @PostMapping("/{id}/return")
    public String resolveReturn(@PathVariable Long id, @RequestParam boolean accept, RedirectAttributes redirect) {
        try { orderService.resolveReturn(shopId(), id, accept); }
        catch (BusinessException exception) { redirect.addAttribute("error", exception.getMessage()); }
        return "redirect:/vendor/orders/" + id;
    }

    @PostMapping("/{id}/refund")
    public String refund(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
        try { refundService.refund(shopId(), id, SecurityUtil.getCurrentUser().getEmail(), request.getRemoteAddr()); }
        catch (BusinessException exception) { redirect.addAttribute("error", exception.getMessage()); }
        return "redirect:/vendor/orders/" + id;
    }

    @PostMapping("/{id}/cash-refund")
    public String cashRefund(@PathVariable Long id, RedirectAttributes redirect) {
        try { orderService.completeCashRefund(shopId(), id); }
        catch (BusinessException exception) { redirect.addAttribute("error", exception.getMessage()); }
        return "redirect:/vendor/orders/" + id;
    }
}
