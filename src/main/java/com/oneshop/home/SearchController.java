package com.oneshop.home;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final HomeCategoryService categoryService;

    public record Item(Long id, String name, long price, int sold) {}

    private static final List<Item> SAMPLE = List.of(
            new Item(1L, "Dầu gội thảo mộc", 120000, 50),
            new Item(2L, "Dầu xả phục hồi tóc", 135000, 32),
            new Item(3L, "Nước hoa hương gỗ", 650000, 12),
            new Item(4L, "Son môi lì", 180000, 85),
            new Item(5L, "Mascara chống trôi", 210000, 40),
            new Item(6L, "Kem dưỡng ẩm ban đêm", 320000, 27),
            new Item(7L, "Sữa rửa mặt dịu nhẹ", 95000, 120),
            new Item(8L, "Phấn mắt 12 màu", 275000, 18));

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String keyword,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long minPrice,
                         @RequestParam(required = false) Long maxPrice,
                         @RequestParam(defaultValue = "sold") String sort,
                         Model model) {

        String kw = keyword.trim().toLowerCase();

        Comparator<Item> order = switch (sort) {
            case "price_asc" -> Comparator.comparingLong(Item::price);
            case "price_desc" -> Comparator.comparingLong(Item::price).reversed();
            default -> Comparator.comparingInt(Item::sold).reversed();
        };

        List<Item> results = SAMPLE.stream()
                .filter(i -> kw.isEmpty() || i.name().toLowerCase().contains(kw))
                .filter(i -> minPrice == null || i.price() >= minPrice)
                .filter(i -> maxPrice == null || i.price() <= maxPrice)
                .sorted(order)
                .toList();

        model.addAttribute("results", results);
        model.addAttribute("categories", categoryService.activeTree());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        return "web/search";
    }
}