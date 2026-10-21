package com.oneshop.home;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Product;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeProductService {

    private static final int PAGE_SIZE = 8;

    public record Card(Long id, String name, BigDecimal price, int sold, double rating) {}

    public record Detail(Long id, String name, String description, BigDecimal price,
                         int stock, int sold, double rating, String brand,
                         Long shopId, String shopName, Long categoryId, String categoryName) {}

    private final HomeProductRepository repo;

    private static Card card(Product p) {
        return new Card(p.getId(), p.getName(), p.getPrice(), p.getSold(), p.getAvgRating());
    }

    @Transactional(readOnly = true)
    public Page<Card> bestSellers(int page) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "sold"));
        return repo.findVisible(10, ShopStatus.ACTIVE, pr).map(HomeProductService::card);
    }

    @Transactional(readOnly = true)
    public List<Card> suggestions() {
        PageRequest pr = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "avgRating"));
        return repo.findVisible(-1, ShopStatus.ACTIVE, pr).map(HomeProductService::card).getContent();
    }

    @Transactional(readOnly = true)
    public Optional<Detail> detail(Long id) {
        return repo.findVisibleById(id, ShopStatus.ACTIVE).map(p -> new Detail(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getStock(), p.getSold(), p.getAvgRating(), p.getBrand(),
                p.getShop().getId(), p.getShop().getName(),
                p.getCategory().getId(), p.getCategory().getName()));
    }

    @Transactional(readOnly = true)
    public List<Card> related(Detail d) {
        return repo.findRelated(d.categoryId(), d.id(), ShopStatus.ACTIVE, PageRequest.of(0, 4))
                .stream().map(HomeProductService::card).toList();
    }
}