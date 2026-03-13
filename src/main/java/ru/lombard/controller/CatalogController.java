package ru.lombard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.lombard.dto.ProductDto;
import ru.lombard.entity.Product;
import ru.lombard.service.CartService;
import ru.lombard.service.CategoryService;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.ProductService;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String catalog(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Product.Condition cond = null;
        if (condition != null && !condition.isEmpty()) {
            try {
                cond = Product.Condition.valueOf(condition.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        Page<ProductDto> products = productService.findPublished(categoryId, minPrice, maxPrice, cond, search, page);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAllRoot());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("condition", condition);
        model.addAttribute("search", search);
        Long userId = currentUserService.getCurrentUserIdOrNull();
        if (userId != null) {
            model.addAttribute("cartCount", cartService.getCartCount(userId));
        }
        return "catalog";
    }

    @GetMapping("/product/{slug}")
    public String product(@PathVariable String slug, Model model) {
        Product product = productService.getBySlugAndIncrementViews(slug);
        if (product == null) {
            return "redirect:/catalog";
        }
        model.addAttribute("product", productService.toDto(product));
        Long userId = currentUserService.getCurrentUserIdOrNull();
        if (userId != null) {
            model.addAttribute("cartCount", cartService.getCartCount(userId));
        }
        return "product";
    }
}
