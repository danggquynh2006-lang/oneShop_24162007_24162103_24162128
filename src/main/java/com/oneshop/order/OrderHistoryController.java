package com.oneshop.order;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderHistoryController {

    public record OrderRow(Long id, String shopName, String statusLabel, long total) {}

    @GetMapping("/user/orders")
    public String orders(@RequestParam(defaultValue = "NEW") OrderTab tab, Model model) {
        model.addAttribute("tabs", OrderTab.values());
        model.addAttribute("current", tab);
        model.addAttribute("orders", List.of(
                new OrderRow(1001L, "Shop mẫu 1", tab.getLabel(), 398000),
                new OrderRow(1002L, "Shop mẫu 2", tab.getLabel(), 199000)));
        return "web/orders";
    }
}