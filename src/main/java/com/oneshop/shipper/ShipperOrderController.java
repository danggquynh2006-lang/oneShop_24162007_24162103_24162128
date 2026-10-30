package com.oneshop.shipper;

import java.time.LocalDate;
import java.util.List;

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
import com.oneshop.entity.User;

import lombok.RequiredArgsConstructor;

/** /shipper/** - chỉ role SHIPPER (đã cấu hình trong SecurityConfig). */
@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
public class ShipperOrderController {

    /** Các tab trạng thái hiển thị cho shipper. */
    private static final List<OrderStatus> TABS = List.of(
            OrderStatus.READY_TO_SHIP,
            OrderStatus.PICKED_UP,
            OrderStatus.SHIPPING,
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED);

    private final ShipperOrderService orderService;

    private Long shipperId() {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            throw new BusinessException("Vui lòng đăng nhập.");
        }
        return user.getId();
    }

    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/shipper/orders";
    }

    @GetMapping("/orders")
    public String list(@RequestParam(required = false) OrderStatus status,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("orders", orderService.list(shipperId(), status, page));
        model.addAttribute("tabs", TABS);
        model.addAttribute("selected", status);
        return "shipper/orders";
    }

    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String msg,
                         @RequestParam(required = false) String error, Model model) {
        model.addAttribute("order", orderService.get(shipperId(), id));
        model.addAttribute("items", orderService.items(id));
        model.addAttribute("payment", orderService.payment(id));
        if ("pickup".equals(msg)) {
            model.addAttribute("message", "Đã xác nhận lấy hàng.");
        } else if ("start".equals(msg)) {
            model.addAttribute("message", "Đơn đang được giao.");
        } else if ("delivered".equals(msg)) {
            model.addAttribute("message", "Đã giao hàng thành công.");
        } else if ("failed".equals(msg)) {
            model.addAttribute("message", "Đã ghi nhận giao thất bại, tồn kho đã được hoàn lại.");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "shipper/order-detail";
    }

    @PostMapping("/orders/{id}/pickup")
    public String pickUp(@PathVariable Long id, RedirectAttributes redirect) {
        return act(id, "pickup", redirect, () -> orderService.pickUp(shipperId(), id));
    }

    @PostMapping("/orders/{id}/start")
    public String start(@PathVariable Long id, RedirectAttributes redirect) {
        return act(id, "start", redirect, () -> orderService.startShipping(shipperId(), id));
    }

    @PostMapping("/orders/{id}/delivered")
    public String delivered(@PathVariable Long id, RedirectAttributes redirect) {
        return act(id, "delivered", redirect, () -> orderService.delivered(shipperId(), id));
    }

    @PostMapping("/orders/{id}/failed")
    public String failed(@PathVariable Long id, RedirectAttributes redirect) {
        return act(id, "failed", redirect, () -> orderService.failed(shipperId(), id));
    }

    @GetMapping("/statistics")
    public String statistics(@RequestParam(required = false) Integer year,
                             @RequestParam(required = false) Integer month, Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("stats", orderService.statistics(shipperId(),
                year == null ? today.getYear() : year,
                month == null ? today.getMonthValue() : month));
        return "shipper/statistics";
    }

    private String act(Long id, String msg, RedirectAttributes redirect, Runnable action) {
        try {
            action.run();
            redirect.addAttribute("msg", msg);
        } catch (BusinessException e) {
            redirect.addAttribute("error", e.getMessage());
        }
        return "redirect:/shipper/orders/" + id;
    }
}