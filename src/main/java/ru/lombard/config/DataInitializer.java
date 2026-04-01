package ru.lombard.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.lombard.entity.Category;
import ru.lombard.entity.Product;
import ru.lombard.entity.User;
import ru.lombard.repository.CategoryRepository;
import ru.lombard.repository.ProductRepository;
import ru.lombard.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        User admin = ensureDefaultUsers();
        ensureDemoProductsForLeafCategories(admin);
    }

    private User ensureDefaultUsers() {
        User admin = userRepository.findByEmail("admin@lombard.ru")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin@lombard.ru")
                        .passwordHash(passwordEncoder.encode("password"))
                        .fullName("Администратор")
                        .role(User.Role.ADMIN)
                        .blocked(false)
                        .build()));

        userRepository.findByEmail("manager@lombard.ru")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("manager@lombard.ru")
                        .passwordHash(passwordEncoder.encode("password"))
                        .fullName("Менеджер")
                        .role(User.Role.MANAGER)
                        .blocked(false)
                        .build()));

        return admin;
    }

    private void ensureDemoProductsForLeafCategories(User author) {
        List<Category> categories = categoryRepository.findAll();
        Set<Long> parentIds = categories.stream()
                .map(Category::getParent)
                .filter(parent -> parent != null && parent.getId() != null)
                .map(Category::getId)
                .collect(Collectors.toSet());

        List<Category> leafCategories = categories.stream()
                .filter(category -> !parentIds.contains(category.getId()))
                .toList();

        for (Category category : leafCategories) {
            long existing = productRepository.countByCategoryId(category.getId());
            for (int i = (int) existing + 1; i <= 2; i++) {
                productRepository.save(buildDemoProduct(category, author, i));
            }
        }
    }

    private Product buildDemoProduct(Category category, User author, int index) {
        String slug = nextAvailableSlug(category.getSlug() + "-demo-" + index);
        return Product.builder()
                .category(category)
                .createdBy(author)
                .name(category.getName() + " - демо товар " + index)
                .slug(slug)
                .description("Демо-товар для категории \"" + category.getName() + "\". Фото можно добавить в админке.")
                .condition(index % 2 == 0 ? Product.Condition.NEW : Product.Condition.USED)
                .price(BigDecimal.valueOf(15000L + (long) index * 3500L))
                .quantity(1)
                .status(Product.ProductStatus.PUBLISHED)
                .year(2020 + index)
                .publishedAt(Instant.now())
                .build();
    }

    private String nextAvailableSlug(String baseSlug) {
        String slug = baseSlug;
        int suffix = 2;
        while (productRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }
}
