package com.oneshop.favorite;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.oneshop.auth.SecurityUtil;
import com.oneshop.entity.User;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final ViewedService viewedService;

    @GetMapping("/user/favorites")
    public String favorites(Model model) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("items", favoriteService.list(user));
        return "web/favorites";
    }

    @GetMapping("/user/viewed")
    public String viewed(Model model) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("items", viewedService.recent(user));
        return "web/viewed";
    }
}