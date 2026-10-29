package com.oneshop.favorite;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.entity.User;
import com.oneshop.entity.ViewedProduct;
import com.oneshop.home.HomeProductRepository;
import com.oneshop.repository.ViewedProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ViewedService {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final ViewedProductRepository viewedRepo;
    private final HomeProductRepository productRepo;

    @Transactional
    public void record(User user, Long productId) {
        var existing = viewedRepo.findByUserIdAndProductId(user.getId(), productId);
        if (existing.isPresent()) {
            existing.get().setViewedAt(LocalDateTime.now());
            return;
        }
        viewedRepo.save(ViewedProduct.builder()
                .user(user)
                .product(productRepo.getReferenceById(productId))
                .build());
    }

    @Transactional(readOnly = true)
    public List<FavoriteService.Item> recent(User user) {
        return viewedRepo.findRecent(user.getId(), PageRequest.of(0, 20)).stream()
                .filter(v -> v.getProduct().isActive() && !v.getProduct().isLocked())
                .map(v -> new FavoriteService.Item(v.getProduct().getId(), v.getProduct().getName(),
                        v.getProduct().getPrice(), "Xem lúc " + TIME.format(v.getViewedAt())))
                .toList();
    }
}