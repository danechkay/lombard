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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import ru.lombard.dto.ProductDto;
import ru.lombard.entity.Product;
import ru.lombard.entity.Store;
import ru.lombard.entity.User;
import ru.lombard.repository.StoreRepository;
import ru.lombard.service.CategoryService;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.ProductService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductApiController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final StoreRepository storeRepository;
    private final CurrentUserService currentUserService;

    @GetMapping
    public Page<ProductDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 500);
        User user = currentUserService.getCurrentUser().orElseThrow();
        if (user.getRole() == User.Role.ADMIN) {
            return productService.findAllForAdmin(page, safeSize).map(productService::toDto);
        }
        return productService.findAllForStoreAdmin(requireManagerStoreId(user), page, safeSize).map(productService::toDto);
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser().orElseThrow();
        Product product = productService.findById(id).orElseThrow();
        verifyProductAccess(user, product);
        return productService.toDto(product);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Long categoryId,
            @RequestParam Long storeId,
            @RequestParam String condition,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) MultipartFile[] images
    ) throws Exception {
        User user = currentUserService.getCurrentUser().orElseThrow();
        var category = categoryService.findById(categoryId).orElseThrow();
        Long effectiveStoreId = resolveWritableStoreId(user, storeId);
        Store store = storeRepository.findById(effectiveStoreId)
                .filter(Store::isActive)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Некорректный магазин"));
        Product product = Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .store(store)
                .condition(parseEnum(Product.Condition.class, condition, "Некорректное состояние товара"))
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
            @RequestParam Long storeId,
            @RequestParam String condition,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) MultipartFile[] images
    ) throws Exception {
        User user = currentUserService.getCurrentUser().orElseThrow();
        Product product = productService.findById(id).orElseThrow();
        verifyProductAccess(user, product);
        var category = categoryService.findById(categoryId).orElseThrow();
        Long effectiveStoreId = resolveWritableStoreId(user, storeId);
        Store store = storeRepository.findById(effectiveStoreId)
                .filter(Store::isActive)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Некорректный магазин"));
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setStore(store);
        product.setCondition(parseEnum(Product.Condition.class, condition, "Некорректное состояние товара"));
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setYear(year);
        if (status != null && !status.isBlank()) {
            try {
                product.setStatus(parseEnum(Product.ProductStatus.class, status, "Некорректный статус товара"));
            } catch (ResponseStatusException ex) {
                // Если пришёл некорректный статус из SPA, просто игнорируем его
                // и оставляем текущий статус товара, чтобы не падать с 400.
            }
        }
        productService.update(product, images != null ? List.of(images) : new ArrayList<>());
        return Map.of("message", "Товар обновлен");
    }

    @PostMapping("/{id}/publish")
    public Map<String, String> publish(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser().orElseThrow();
        Product product = productService.findById(id).orElseThrow();
        verifyProductAccess(user, product);
        productService.publish(id);
        return Map.of("message", "Товар опубликован");
    }

    @PostMapping("/{id}/unpublish")
    public Map<String, String> unpublish(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser().orElseThrow();
        Product product = productService.findById(id).orElseThrow();
        verifyProductAccess(user, product);
        productService.unpublish(id);
        return Map.of("message", "Товар снят с публикации");
    }

    @PostMapping("/{id}/delete")
    public Map<String, String> delete(@PathVariable Long id) throws Exception {
        User user = currentUserService.getCurrentUser().orElseThrow();
        Product product = productService.findById(id).orElseThrow();
        verifyProductAccess(user, product);
        productService.delete(id);
        return Map.of("message", "Товар удален");
    }

    private void verifyProductAccess(User user, Product product) {
        if (user.getRole() == User.Role.ADMIN) {
            return;
        }
        Long managerStoreId = requireManagerStoreId(user);
        Long productStoreId = product.getStore() != null ? product.getStore().getId() : null;
        if (!managerStoreId.equals(productStoreId)) {
            throw new ResponseStatusException(FORBIDDEN, "Нет доступа к товару другого магазина");
        }
    }

    private Long resolveWritableStoreId(User user, Long requestedStoreId) {
        if (user.getRole() == User.Role.ADMIN) {
            return requestedStoreId;
        }
        Long managerStoreId = requireManagerStoreId(user);
        if (requestedStoreId != null && !managerStoreId.equals(requestedStoreId)) {
            throw new ResponseStatusException(FORBIDDEN, "Менеджер может работать только со своим магазином");
        }
        return managerStoreId;
    }

    private Long requireManagerStoreId(User user) {
        if (user.getStore() == null || user.getStore().getId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Менеджер не привязан к магазину");
        }
        return user.getStore().getId();
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, message);
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, message);
        }
    }
}
