package com.oneshop.cart;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.entity.User;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    @FunctionalInterface
    private interface Action {
        void run(User user);
    }

    private ResponseEntity<Map<String, Object>> handle(Action action) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "message", "Vui lòng đăng nhập"));
        }
        try {
            action.run(user);
            return ResponseEntity.ok(Map.of("ok", true, "count", cartService.count(user)));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(@RequestParam Long productId,
                                                   @RequestParam(defaultValue = "1") int quantity) {
        return handle(user -> cartService.add(user, productId, quantity));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestParam Long itemId,
                                                      @RequestParam int quantity) {
        return handle(user -> cartService.updateQuantity(user, itemId, quantity));
    }

    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> remove(@RequestParam Long itemId) {
        return handle(user -> cartService.remove(user, itemId));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> count() {
        return handle(user -> { });
    }
}