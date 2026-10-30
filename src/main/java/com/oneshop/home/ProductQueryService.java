package com.oneshop.home;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Product;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    public static final int PAGE_SIZE = 8;

    public record Filter(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                         Long shopId, String brand, Double minRating, String sort) {}

    public record ShopOption(Long id, String name) {}

    private final HomeProductRepository repo;
    private final HomeCategoryRepository categoryRepo;
    private final ProductImages images;

    private static Sort sortOf(String sort) {
        return switch (sort == null ? "sold" : sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price").and(Sort.by("id"));
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price").and(Sort.by("id"));
            case "new" -> Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"));
            case "rating" -> Sort.by(Sort.Direction.DESC, "avgRating")
                    .and(Sort.by(Sort.Direction.DESC, "sold"));
            default -> Sort.by(Sort.Direction.DESC, "sold").and(Sort.by("id"));
        };
    }

    private List<Long> categoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        categoryRepo.findActiveChildren(categoryId).forEach(c -> ids.add(c.getId()));
        return ids;
    }

    @Transactional(readOnly = true)
    public Page<HomeProductService.Card> search(Filter f, int page) {
        Specification<Product> spec = ProductSpecs.visible()
                .and(ProductSpecs.nameContains(f.keyword()))
                .and(ProductSpecs.inCategories(f.categoryId() == null ? null : categoryIds(f.categoryId())))
                .and(ProductSpecs.priceBetween(f.minPrice(), f.maxPrice()))
                .and(ProductSpecs.inShop(f.shopId()))
                .and(ProductSpecs.brandIs(f.brand()))
                .and(ProductSpecs.ratingAtLeast(f.minRating()));

        PageRequest pr = PageRequest.of(Math.max(page, 0), PAGE_SIZE, sortOf(f.sort()));
        Page<Product> result = repo.findAll(spec, pr);
        Map<Long, String> imgs = images.mainUrls(result.getContent().stream().map(Product::getId).toList());
        return result.map(p -> new HomeProductService.Card(
                p.getId(), p.getName(), p.getPrice(), p.getSold(), p.getAvgRating(), imgs.get(p.getId())));
    }

    @Transactional(readOnly = true)
    public List<String> brands() {
        return repo.findBrands(ShopStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<ShopOption> shops() {
        return repo.findShops(ShopStatus.ACTIVE).stream()
                .map(r -> new ShopOption((Long) r[0], (String) r[1]))
                .toList();
    }
}