package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lombard.dto.CategoryDto;
import ru.lombard.entity.Category;
import ru.lombard.repository.CategoryRepository;
import ru.lombard.repository.ProductRepository;

import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryDto> findAllRoot() {
        return categoryRepository.findByParentIsNullOrderBySortOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<CategoryDto> findAllOrdered() {
        return categoryRepository.findAll().stream()
                .sorted((a, b) -> {
                    int parentA = a.getParent() == null ? 0 : 1;
                    int parentB = b.getParent() == null ? 0 : 1;
                    if (parentA != parentB) return parentA - parentB;
                    int sa = a.getSortOrder();
                    int sb = b.getSortOrder();
                    if (sa != sb) return sa - sb;
                    return a.getId().compareTo(b.getId());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Long> collectCategoryTreeIds(Long rootCategoryId) {
        if (rootCategoryId == null) return List.of();
        List<Category> all = categoryRepository.findAll();
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (Category c : all) {
            if (c.getParent() == null) continue;
            childrenByParent.computeIfAbsent(c.getParent().getId(), k -> new ArrayList<>()).add(c.getId());
        }

        List<Long> ids = new ArrayList<>();
        ArrayDeque<Long> stack = new ArrayDeque<>();
        stack.push(rootCategoryId);

        while (!stack.isEmpty()) {
            Long id = stack.pop();
            ids.add(id);
            List<Long> children = childrenByParent.get(id);
            if (children == null) continue;
            for (Long childId : children) {
                stack.push(childId);
            }
        }
        return ids;
    }

    public List<Category> findAllRootEntities() {
        return categoryRepository.findByParentIsNullOrderBySortOrderAsc();
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Transactional
    public Category save(Category category) {
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(slugify(category.getName()));
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteById(Long id) {
        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalStateException("Нельзя удалить категорию с подкатегориями");
        }
        if (productRepository.countByCategoryId(id) > 0) {
            throw new IllegalStateException("Нельзя удалить категорию, в которой есть товары");
        }
        categoryRepository.deleteById(id);
    }

    public CategoryDto toDto(Category c) {
        return CategoryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .sortOrder(c.getSortOrder())
                .build();
    }

    private static final String CYR = "абвгдеёжзийклмнопрстуфхцчшщыэюя";
    private static final String LAT = "abvgdeejzijklmnoprstufhzcssyeyua";

    public static String slugify(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toLowerCase().toCharArray()) {
            int i = CYR.indexOf(c);
            if (i >= 0) sb.append(LAT.charAt(i));
            else if (Character.isLetterOrDigit(c) && c < 128) sb.append(c);
            else if (c == ' ' || c == '-') sb.append('-');
        }
        return sb.toString().replaceAll("-+", "-").replaceAll("^-|-$", "");
    }
}
