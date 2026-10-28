package com.oneshop.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import com.oneshop.common.OrderStatus;
import com.oneshop.common.PaymentMethod;
import com.oneshop.common.PaymentStatus;
import com.oneshop.entity.User;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OrderHistoryController {

    private final UserOrderService orderService;
    private final VnpayAvailability vnpay;

    private static String label(PaymentStatus s) {
        return switch (s) {
            case UNPAID -> "Chưa thanh toán";
            case PAID -> "Đã thanh toán";
            case FAILED -> "Thanh toán thất bại";
            case REFUND_PENDING -> "Chờ hoàn tiền";
            case REFUNDED -> "Đã hoàn tiền";
            default -> s.name();
        };
    }

    @GetMapping("/user/orders")
    public String orders(@RequestParam(defaultValue = "NEW") OrderTab tab, Model model) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("tabs", OrderTab.values());
        model.addAttribute("current", tab);
        model.addAttribute("orders", orderService.list(user, tab));
        return "web/orders";
    }

    @GetMapping("/user/orders/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        try {
            var order = orderService.detail(user, id);
            PaymentStatus pay = orderService.paymentStatus(id).orElse(null);

            boolean vnpayOrder = order.getPaymentMethod() == PaymentMethod.VNPAY;
            boolean open = order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.CONFIRMED;
            boolean unpaid = pay == null || pay == PaymentStatus.UNPAID || pay == PaymentStatus.FAILED;

            String paymentLabel = null;
            if (pay != null) {
                paymentLabel = label(pay);
            } else if (vnpayOrder) {
                paymentLabel = "Chưa thanh toán";
            }

            model.addAttribute("order", order);
            model.addAttribute("items", orderService.items(id));
            model.addAttribute("paymentLabel", paymentLabel);
            model.addAttribute("canPay", vnpayOrder && open && unpaid && vnpay.enabled());
            model.addAttribute("canCancel", OrderRules.userCanCancel(order.getStatus()));
            model.addAttribute("canReturn", OrderRules.userCanRequestReturn(order.getStatus()));
            model.addAttribute("returnReasons", UserOrderService.REASONS);
            model.addAttribute("returnInfo", orderService.returnInfo(id).orElse(null));
            return "web/order-detail";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/orders";
        }
    }

    @PostMapping("/user/orders/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        try {
            orderService.cancel(user, id);
            ra.addFlashAttribute("message", "Đã hủy đơn hàng #" + id + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/orders/" + id;
    }

    @PostMapping("/user/orders/{id}/return")
    public String requestReturn(@PathVariable Long id,
                                @RequestParam(required = false) String reason,
                                @RequestParam(required = false) String note,
                                RedirectAttributes ra) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        try {
            orderService.requestReturn(user, id, reason, note);
            ra.addFlashAttribute("message", "Đã gửi yêu cầu trả hàng cho đơn #" + id + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/orders/" + id;
    }
}