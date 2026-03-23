package ru.lombard.controller.api.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.lombard.dto.ProductDto;
import ru.lombard.entity.Product;
import ru.lombard.entity.User;
import ru.lombard.service.CategoryService;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.ProductService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductApiController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public Page<ProductDto> list(@RequestParam(defaultValue = "0") int page) {
        return productService.findAllForAdmin(page, 20).map(productService::toDto);
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable Long id) {
        Product product = productService.findById(id).orElseThrow();
        return productService.toDto(product);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Long categoryId,
            @RequestParam String condition,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) MultipartFile[] images
    ) throws Exception {
        User user = currentUserService.getCurrentUser().orElseThrow();
        var category = categoryService.findById(categoryId).orElseThrow();
        Product product = Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .condition(Product.Condition.valueOf(condition.toUpperCase()))
                .price(price)
                .quantity(quantity)
                .year(year)
                .status(Product.ProductStatus.DRAFT)
                .build();
        productService.create(product, user, images != null ? List.of(images) : new ArrayList<>());
        return Map.of("message", "Товар создан");
    }

    @PostMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Long categoryId,
            @RequestParam String condition,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) Integer year,
            @RequestParam String status,
            @RequestParam(required = false) MultipartFile[] images
    ) throws Exception {
        Product product = productService.findById(id).orElseThrow();
        var category = categoryService.findById(categoryId).orElseThrow();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setCondition(Product.Condition.valueOf(condition.toUpperCase()));
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setYear(year);
        product.setStatus(Product.ProductStatus.valueOf(status.toUpperCase()));
        productService.update(product, images != null ? List.of(images) : new ArrayList<>());
        return Map.of("message", "Товар обновлен");
    }

    @PostMapping("/{id}/publish")
    public Map<String, String> publish(@PathVariable Long id) {
        productService.publish(id);
        return Map.of("message", "Товар опубликован");
    }

    @PostMapping("/{id}/unpublish")
    public Map<String, String> unpublish(@PathVariable Long id) {
        productService.unpublish(id);
        return Map.of("message", "Товар снят с публикации");
    }
}
