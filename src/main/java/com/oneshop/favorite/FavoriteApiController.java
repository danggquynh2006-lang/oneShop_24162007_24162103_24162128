package com.oneshop.favorite;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.common.BusinessException;
import com.oneshop.entity.User;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteApiController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@RequestParam Long productId) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "message", "Vui lòng đăng nhập"));
        }
        try {
            boolean favorited = favoriteService.toggle(user, productId);
            return ResponseEntity.ok(Map.of("ok", true, "favorited", favorited));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", e.getMessage()));
        }
    }
}