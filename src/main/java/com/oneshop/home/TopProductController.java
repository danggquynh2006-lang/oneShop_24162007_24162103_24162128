package com.oneshop.home;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TopProductController {

    private static final int PAGE_SIZE = 8;
    private static final int TOP = 20;

    public enum Tab {
        NEW("Mới nhất"), BEST("Bán chạy"), RATED("Đánh giá cao"), LOVED("Yêu thích nhiều");

        private final String label;
        Tab(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public record Item(Long id, String name, long price, int sold,
                       double rating, int favorites, int daysOld) {}

    @GetMapping("/top20")
    public String top20(@RequestParam(defaultValue = "BEST") Tab tab,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {

        List<Item> all = IntStream.rangeClosed(1, 30)
                .mapToObj(i -> new Item((long) i, "Sản phẩm mẫu " + i,
                        90000L + (i * 7 % 11) * 25000L,
                        (i * 37) % 150,
                        3.0 + (i * 13 % 21) / 10.0,
                        (i * 17) % 90,
                        (i * 5) % 40))
                .toList();

        Comparator<Item> order = switch (tab) {
            case NEW -> Comparator.comparingInt(Item::daysOld);
            case RATED -> Comparator.comparingDouble(Item::rating).reversed();
            case LOVED -> Comparator.comparingInt(Item::favorites).reversed();
            default -> Comparator.comparingInt(Item::sold).reversed();
        };

        List<Item> top = all.stream().sorted(order).limit(TOP).toList();

        int totalPages = (int) Math.ceil(top.size() / (double) PAGE_SIZE);
        int current = Math.max(0, Math.min(page, totalPages - 1));
        int from = current * PAGE_SIZE;
        List<Item> items = top.subList(from, Math.min(from + PAGE_SIZE, top.size()));

        model.addAttribute("tabs", Tab.values());
        model.addAttribute("current", tab);
        model.addAttribute("items", items);
        model.addAttribute("page", current);
        model.addAttribute("totalPages", totalPages);
        return "web/top20";
    }
}