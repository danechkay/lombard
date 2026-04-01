package ru.lombard.controller.api.admin;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.lombard.dto.CategoryDto;
import ru.lombard.entity.Category;
import ru.lombard.service.CategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryApiController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> list() {
        return categoryService.findAllOrdered();
    }

    @PostMapping
    public Map<String, String> create(@RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder());
        if (request.getParentId() != null && request.getParentId() > 0) {
            categoryService.findById(request.getParentId()).ifPresent(category::setParent);
        }
        categoryService.save(category);
        return Map.of("message", "Категория создана");
    }

    @PostMapping("/{id}")
    public Map<String, String> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        Category category = categoryService.findById(id).orElseThrow();
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder());
        if (request.getParentId() != null && request.getParentId() > 0) {
            categoryService.findById(request.getParentId()).ifPresent(category::setParent);
        } else {
            category.setParent(null);
        }
        categoryService.save(category);
        return Map.of("message", "Категория обновлена");
    }

    @PostMapping("/{id}/delete")
    public Map<String, String> delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return Map.of("message", "Категория удалена");
    }

    @Data
    public static class CategoryRequest {
        private String name;
        private Long parentId;
        private int sortOrder;
    }
}
