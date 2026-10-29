package com.oneshop.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductImages {

    private final HomeImageRepository repo;

    public Map<Long, String> mainUrls(Collection<Long> productIds) {
        Map<Long, String> map = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return map;
        }
        for (Object[] row : repo.findUrls(productIds)) {
            map.putIfAbsent((Long) row[0], (String) row[1]);
        }
        return map;
    }

    public List<String> allUrls(Long productId) {
        List<String> urls = new ArrayList<>();
        for (Object[] row : repo.findUrls(List.of(productId))) {
            urls.add((String) row[1]);
        }
        return urls;
    }
}