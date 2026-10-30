package com.oneshop.favorite;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.BusinessException;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Favorite;
import com.oneshop.entity.Product;
import com.oneshop.entity.User;
import com.oneshop.home.HomeProductRepository;
import com.oneshop.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    public record Item(Long id, String name, BigDecimal price, String note) {}

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FavoriteRepository favoriteRepo;
    private final HomeProductRepository productRepo;

    @Transactional
    public boolean toggle(User user, Long productId) {
        var existing = favoriteRepo.findByUserIdAndProductId(user.getId(), productId);
        if (existing.isPresent()) {
            favoriteRepo.delete(existing.get());
            return false;
        }
        Product product = productRepo.findVisibleById(productId, ShopStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Sản phẩm không tồn tại hoặc không còn được bán."));
        favoriteRepo.save(Favorite.builder().user(user).product(product).build());
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(User user, Long productId) {
        return favoriteRepo.existsByUserIdAndProductId(user.getId(), productId);
    }

    @Transactional(readOnly = true)
    public List<Item> list(User user) {
        return favoriteRepo.findByUser(user.getId()).stream()
                .filter(f -> f.getProduct().isActive() && !f.getProduct().isLocked())
                .map(f -> new Item(f.getProduct().getId(), f.getProduct().getName(),
                        f.getProduct().getPrice(),
                        f.getCreatedAt() == null ? "" : "Thêm ngày " + DATE.format(f.getCreatedAt())))
                .toList();
    }
}