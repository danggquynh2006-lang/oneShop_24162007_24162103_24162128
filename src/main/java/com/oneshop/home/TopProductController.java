package com.oneshop.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TopProductController {

    private final TopProductService topService;

    @GetMapping("/top20")
    public String top20(@RequestParam(defaultValue = "BEST") TopProductService.Tab tab,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        var result = topService.top(tab, page);
        model.addAttribute("tabs", TopProductService.Tab.values());
        model.addAttribute("current", tab);
        model.addAttribute("items", result.items());
        model.addAttribute("page", result.page());
        model.addAttribute("totalPages", result.totalPages());
        return "web/top20";
    }
}