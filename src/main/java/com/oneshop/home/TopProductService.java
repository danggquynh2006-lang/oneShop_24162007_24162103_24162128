package com.oneshop.home;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Product;
import com.oneshop.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TopProductService {

    public static final int TOP = 20;
    public static final int PAGE_SIZE = 8;

    public enum Tab {
        NEW("Mới nhất"), BEST("Bán chạy"), RATED("Đánh giá cao"), LOVED("Yêu thích nhiều");

        private final String label;

        Tab(String label) { this.label = label; }

        public String getLabel() { return label; }
    }

    public record Entry(Long id, String name, BigDecimal price, int sold, double rating, long favorites) {}

    public record TopPage(List<Entry> items, int page, int totalPages) {}

    private final HomeProductRepository repo;
    private final FavoriteRepository favoriteRepo;

    @Transactional(readOnly = true)
    public TopPage top(Tab tab, int page) {
        List<Product> top = switch (tab) {
            case NEW -> visible(Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")));
            case RATED -> visible(Sort.by(Sort.Direction.DESC, "avgRating")
                    .and(Sort.by(Sort.Direction.DESC, "sold")));
            case LOVED -> repo.findMostLoved(ShopStatus.ACTIVE, PageRequest.of(0, TOP));
            default -> visible(Sort.by(Sort.Direction.DESC, "sold").and(Sort.by("id")));
        };

        int totalPages = Math.max(1, (int) Math.ceil(top.size() / (double) PAGE_SIZE));
        int current = Math.max(0, Math.min(page, totalPages - 1));
        int from = current * PAGE_SIZE;

        List<Entry> items = top.subList(from, Math.min(from + PAGE_SIZE, top.size())).stream()
                .map(p -> new Entry(p.getId(), p.getName(), p.getPrice(), p.getSold(),
                        p.getAvgRating(), favoriteRepo.countByProductId(p.getId())))
                .toList();
        return new TopPage(items, current, totalPages);
    }

    private List<Product> visible(Sort sort) {
        return repo.findVisible(-1, ShopStatus.ACTIVE, PageRequest.of(0, TOP, sort)).getContent();
    }
}