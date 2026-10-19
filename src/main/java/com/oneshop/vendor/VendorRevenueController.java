package com.oneshop.vendor;

import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.oneshop.auth.SecurityUtil;
import lombok.RequiredArgsConstructor;

@Controller @RequestMapping("/vendor/revenue") @RequiredArgsConstructor
public class VendorRevenueController {
    private final VendorRevenueService revenueService;
    private final ShopService shopService;

    @GetMapping
    public String revenue(@RequestParam(required = false) Integer year,
                          @RequestParam(required = false) Integer month,
                          @RequestParam(required = false) Integer day,
                          @RequestParam(defaultValue = "MONTH") String period, Model model) {
        LocalDate today = LocalDate.now();
        int chosenYear = year == null ? today.getYear() : year;
        int chosenMonth = month == null ? today.getMonthValue() : month;
        int chosenDay = day == null ? Math.min(today.getDayOfMonth(),
                LocalDate.of(chosenYear, chosenMonth, 1).lengthOfMonth()) : day;
        Long shopId = shopService.requireActiveShop(SecurityUtil.getCurrentUser()).getId();
        model.addAttribute("year", chosenYear);
        model.addAttribute("month", chosenMonth);
        model.addAttribute("day", chosenDay);
        model.addAttribute("period", period);
        model.addAttribute("summary", switch (period) {
            case "DAY" -> revenueService.daily(shopId, LocalDate.of(chosenYear, chosenMonth, chosenDay));
            case "YEAR" -> revenueService.annual(shopId, chosenYear);
            default -> revenueService.summary(shopId, chosenYear, chosenMonth);
        });
        return "vendor/revenue";
    }
}
