package com.oneshop.favorite;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FavoriteController {

    public record Item(Long id, String name, long price, String note) {}

    @GetMapping("/user/favorites")
    public String favorites(Model model) {
        model.addAttribute("items", List.of(
                new Item(1L, "Sản phẩm mẫu 1", 199000, "Thêm ngày 15/10/2026"),
                new Item(2L, "Sản phẩm mẫu 2", 250000, "Thêm ngày 14/10/2026"),
                new Item(3L, "Sản phẩm mẫu 3", 120000, "Thêm ngày 12/10/2026")));
        return "web/favorites";
    }

    @GetMapping("/user/viewed")
    public String viewed(Model model) {
        model.addAttribute("items", List.of(
                new Item(4L, "Sản phẩm mẫu 4", 99000, "Xem lúc 10:20 hôm nay"),
                new Item(5L, "Sản phẩm mẫu 5", 450000, "Xem lúc 09:05 hôm nay"),
                new Item(6L, "Sản phẩm mẫu 6", 175000, "Xem hôm qua")));
        return "web/viewed";
    }
}