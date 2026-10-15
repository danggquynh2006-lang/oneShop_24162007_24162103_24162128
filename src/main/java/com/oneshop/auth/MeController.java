package com.oneshop.auth;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneshop.entity.User;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, Object> me() {
        User user = SecurityUtil.getCurrentUser();
        return Map.of("email", user.getEmail(),
                      "fullName", String.valueOf(user.getFullName()),
                      "role", user.getRole());
    }
}