package ru.lombard.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.lombard.dto.CategoryDto;
import ru.lombard.dto.ProductDto;
import ru.lombard.dto.StoreDto;
import ru.lombard.entity.Product;
import ru.lombard.service.CategoryService;
import ru.lombard.service.ProductService;
import ru.lombard.service.StoreService;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogApiController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final StoreService storeService;

    @GetMapping
    public Page<ProductDto> catalog(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page
    ) {
        Product.Condition cond = null;
        if (condition != null && !condition.isEmpty()) {
            try {
                cond = Product.Condition.valueOf(condition.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                cond = null;
            }
        }
        return productService.findPublished(categoryId, storeId, minPrice, maxPrice, cond, search, page);
    }

    @GetMapping("/stores")
    public List<StoreDto> stores() {
        return storeService.listActive();
    }

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return categoryService.findAllOrdered();
    }

    @GetMapping("/product/{slug}")
    public ProductDto product(@PathVariable String slug) {
        Product product = productService.getBySlugAndIncrementViews(slug);
        if (product == null) {
            throw new ResponseStatusException(NOT_FOUND, "Товар не найден");
        }
        return productService.toDto(product);
    }
}
