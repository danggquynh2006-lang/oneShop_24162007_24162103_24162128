package com.oneshop.home;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.entity.Product;
import com.oneshop.entity.ProductImage;
import com.oneshop.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageService {
    private final ProductImageRepository images;

    @Transactional(readOnly = true)
    public Map<Long, String> mainUrls(Collection<Product> products) {
        if (products.isEmpty()) return Map.of();
        var productIds = products.stream().map(Product::getId).toList();
        Map<Long, String> urls = new HashMap<>();
        for (ProductImage image : images.findByProductIdInOrderByMainImageDescIdAsc(productIds)) {
            urls.putIfAbsent(image.getProduct().getId(), image.getUrl());
        }
        return urls;
    }
}
