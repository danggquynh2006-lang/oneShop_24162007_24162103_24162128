package com.oneshop.home;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    private final HomeCategoryService categoryService;
    private final ProductQueryService queryService;

    @GetMapping("/category/{id}")
    public String category(@PathVariable Long id,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "sold") String sort,
                           Model model) {
        var category = categoryService.findVisible(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var filter = new ProductQueryService.Filter(null, id, null, null, null, null, null, sort);
        Page<HomeProductService.Card> result = queryService.search(filter, page);

        model.addAttribute("category", category);
        model.addAttribute("products", result);
        model.addAttribute("sort", sort);
        return "web/category";
    }
}