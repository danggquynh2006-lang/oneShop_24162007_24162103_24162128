package com.oneshop.home;

import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final ProductQueryService queryService;
    private final HomeCategoryService categoryService;

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String keyword,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) BigDecimal minPrice,
                         @RequestParam(required = false) BigDecimal maxPrice,
                         @RequestParam(required = false) Long shopId,
                         @RequestParam(required = false) String brand,
                         @RequestParam(required = false) Double minRating,
                         @RequestParam(defaultValue = "sold") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        var filter = new ProductQueryService.Filter(
                keyword, categoryId, minPrice, maxPrice, shopId, brand, minRating, sort);

        UriComponentsBuilder b = UriComponentsBuilder.fromPath("/search");
        if (!keyword.isBlank()) b.queryParam("keyword", keyword.trim());
        if (categoryId != null) b.queryParam("categoryId", categoryId);
        if (minPrice != null) b.queryParam("minPrice", minPrice.toPlainString());
        if (maxPrice != null) b.queryParam("maxPrice", maxPrice.toPlainString());
        if (shopId != null) b.queryParam("shopId", shopId);
        if (brand != null && !brand.isBlank()) b.queryParam("brand", brand.trim());
        if (minRating != null) b.queryParam("minRating", minRating);
        b.queryParam("sort", sort);
        String pageUrlBase = b.build().encode().toUriString() + "&";

        model.addAttribute("products", queryService.search(filter, page));
        model.addAttribute("categories", categoryService.activeTree());
        model.addAttribute("shops", queryService.shops());
        model.addAttribute("brands", queryService.brands());
        model.addAttribute("pageUrlBase", pageUrlBase);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("shopId", shopId);
        model.addAttribute("brand", brand);
        model.addAttribute("minRating", minRating);
        model.addAttribute("sort", sort);
        return "web/search";
    }
}