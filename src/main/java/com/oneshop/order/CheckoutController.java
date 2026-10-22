package com.oneshop.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import com.oneshop.common.PaymentMethod;
import com.oneshop.entity.Carrier;
import com.oneshop.entity.User;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CheckoutAddressRepository addressRepo;
    private final CheckoutCarrierRepository carrierRepo;

    @GetMapping("/user/checkout")
    public String checkout(@RequestParam(required = false) String items,
                           Model model, RedirectAttributes ra) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        List<Long> ids = parseIds(items);

        CheckoutService.Preview preview;
        try {
            preview = checkoutService.preview(user, ids);
        } catch (BusinessException e) {
            ra.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/cart";
        }

        List<Carrier> carriers = carrierRepo.findByActiveTrueOrderByShippingFeeAsc();
        BigDecimal baseFee = carriers.isEmpty() ? BigDecimal.ZERO : carriers.get(0).getShippingFee();
        BigDecimal shippingFee = baseFee.multiply(BigDecimal.valueOf(preview.shops().size()));
        BigDecimal total = PriceCalculator.total(preview.subtotal(), BigDecimal.ZERO,
                BigDecimal.ZERO, shippingFee, BigDecimal.ZERO);

        model.addAttribute("items", joinIds(ids));
        model.addAttribute("addresses", addressRepo.listForUser(user.getEmail()));
        model.addAttribute("shops", preview.shops());
        model.addAttribute("carriers", carriers);
        model.addAttribute("subtotal", preview.subtotal());
        model.addAttribute("shopCount", preview.shops().size());
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);
        return "web/checkout";
    }

    @PostMapping("/user/checkout/place")
    public String place(@RequestParam String items,
                        @RequestParam Long addressId,
                        @RequestParam Long carrierId,
                        @RequestParam(defaultValue = "COD") PaymentMethod payment,
                        @RequestParam(required = false) String voucher,
                        RedirectAttributes ra) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        List<Long> ids = parseIds(items);
        try {
            CheckoutService.Result result =
                    checkoutService.place(user, ids, addressId, carrierId, payment, voucher);
            ra.addFlashAttribute("result", result);
            return "redirect:/user/checkout/success";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/checkout?items=" + joinIds(ids);
        }
    }

    @GetMapping("/user/checkout/success")
    public String success(Model model) {
        if (!model.containsAttribute("result")) {
            return "redirect:/user/orders";
        }
        return "web/checkout-success";
    }

    private static List<Long> parseIds(String items) {
        List<Long> ids = new ArrayList<>();
        if (items == null || items.isBlank()) {
            return ids;
        }
        for (String part : items.split(",")) {
            try {
                Long id = Long.valueOf(part.trim());
                if (!ids.contains(id)) ids.add(id);
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private static String joinIds(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}