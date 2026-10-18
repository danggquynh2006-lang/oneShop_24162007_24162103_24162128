package com.oneshop.home;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    private static final int PAGE_SIZE = 8;

    private final HomeCategoryService categoryService;

    public record Item(Long id, String name, long price, int sold) {}

    @GetMapping("/category/{id}")
    public String category(@PathVariable Long id,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "sold") String sort,
                           Model model) {

        var category = categoryService.findVisible(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Item> all = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> new Item((long) i, category.name() + " - mẫu " + i,
                        80000L + i * 15000L, 100 - i * 3))
                .toList();

        Comparator<Item> order = switch (sort) {
            case "price_asc" -> Comparator.comparingLong(Item::price);
            case "price_desc" -> Comparator.comparingLong(Item::price).reversed();
            default -> Comparator.comparingInt(Item::sold).reversed();
        };
        List<Item> sorted = all.stream().sorted(order).toList();

        int totalPages = (int) Math.ceil(sorted.size() / (double) PAGE_SIZE);
        int current = Math.max(0, Math.min(page, totalPages - 1));
        int from = current * PAGE_SIZE;
        List<Item> items = sorted.subList(from, Math.min(from + PAGE_SIZE, sorted.size()));

        model.addAttribute("category", category);
        model.addAttribute("items", items);
        model.addAttribute("page", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", sorted.size());
        model.addAttribute("sort", sort);
        return "web/category";
    }
}