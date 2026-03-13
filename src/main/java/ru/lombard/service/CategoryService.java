package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lombard.dto.CategoryDto;
import ru.lombard.entity.Category;
import ru.lombard.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> findAllRoot() {
        return categoryRepository.findByParentIsNullOrderBySortOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
