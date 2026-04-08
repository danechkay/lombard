package ru.lombard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.lombard.dto.ProductDto;
import ru.lombard.dto.CategoryDto;
import ru.lombard.entity.Category;
import ru.lombard.entity.Product;
import ru.lombard.service.CartService;
import ru.lombard.service.CategoryService;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.ProductService;
import ru.lombard.service.StoreService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final CurrentUserService currentUserService;
    private final StoreService storeService;

    @GetMapping
    public String catalog(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long storeId,
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
        Page<ProductDto> products = productService.findPublished(categoryId, storeId, minPrice, maxPrice, cond, search, page);
        List<CategoryDto> allCategories = categoryService.findAllOrdered();
        List<CategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
        Map<Long, List<CategoryDto>> childrenByParent = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryDto::getParentId));

        Long selectedRootId = null;
        List<CategoryDto> subCategories = List.of();
        if (categoryId != null) {
            CategoryDto selected = allCategories.stream()
                    .filter(c -> categoryId.equals(c.getId()))
                    .findFirst()
                    .orElse(null);
            if (selected != null) {
                selectedRootId = selected.getParentId() == null ? selected.getId() : selected.getParentId();
            }
            if (selectedRootId != null) {
                subCategories = childrenByParent.getOrDefault(selectedRootId, List.of());
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", allCategories);
        model.addAttribute("rootCategories", rootCategories);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("selectedRootId", selectedRootId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("storeId", storeId);
        model.addAttribute("stores", storeService.listActive());
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

    @GetMapping("/category/{slug}")
    public String catalogByCategorySlug(@PathVariable String slug, RedirectAttributes redirectAttributes) {
        Category category = categoryService.findBySlug(slug).orElse(null);
        if (category == null) {
            return "redirect:/catalog";
        }
        redirectAttributes.addAttribute("categoryId", category.getId());
        redirectAttributes.addAttribute("page", 0);
        return "redirect:/catalog";
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
