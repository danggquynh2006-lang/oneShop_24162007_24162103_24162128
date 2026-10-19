package com.oneshop.payment;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller @RequiredArgsConstructor
public class VnpayController {
    private final VnpayService vnpayService;

    @GetMapping("/payment/vnpay/create")
    public RedirectView create(@RequestParam Long orderId, HttpServletRequest request) {
        if (SecurityUtil.getCurrentUser() == null) throw new BusinessException("Vui lòng đăng nhập.");
        return new RedirectView(vnpayService.createUrl(orderId,
                SecurityUtil.getCurrentUser().getId(), request.getRemoteAddr()));
    }

    @GetMapping("/payment/vnpay/ipn")
    @ResponseBody
    public Map<String, String> ipn(@RequestParam Map<String, String> params) {
        VnpayService.Result result = vnpayService.processCallback(params);
        return Map.of("RspCode", result.code(), "Message", result.message());
    }

    @GetMapping("/payment/vnpay/return")
    public String returned(@RequestParam Map<String, String> params, Model model) {
        VnpayService.Result result = vnpayService.processCallback(params);
        model.addAttribute("result", result);
        return "web/payment-result";
    }
}
