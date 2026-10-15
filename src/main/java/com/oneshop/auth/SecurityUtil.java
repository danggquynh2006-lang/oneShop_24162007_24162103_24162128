package com.oneshop.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.oneshop.entity.User;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUser();
        }
        return null;
    }
}