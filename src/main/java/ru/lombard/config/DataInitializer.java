package ru.lombard.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.lombard.entity.Category;
import ru.lombard.entity.Product;
import ru.lombard.entity.User;
import ru.lombard.entity.Store;
import ru.lombard.repository.CategoryRepository;
import ru.lombard.repository.ProductRepository;
import ru.lombard.repository.StoreRepository;
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
    private final StoreRepository storeRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        User admin = ensureDefaultUsers();
        ensureRequestedCategoryStructure();
        ensureDemoProductsForLeafCategories(admin);
    }

    private void ensureRequestedCategoryStructure() {
        // 1) Корневая категория "Разное" и в ней подкатегория "Драгоценные металлы".
        Category miscRoot = ensureCategory("Разное", "creatures", null, 110);

        Category preciousSub = findFirstCategoryByHints("драгоцен", "precious", "metal")
                .orElseGet(() -> ensureCategory("Драгоценные металлы", "creatures-different", miscRoot, 1));
        moveCategoryIfNeeded(preciousSub, miscRoot, 1, "Драгоценные металлы");

        // 2) Отдельная категория "Украшения" и набор подкатегорий.
        Category jewelryRoot = ensureCategory("Украшения", "jewelry-root", null, 120);

        Category watchesSub = findFirstCategoryByHints("часы", "watch", "clock")
                .orElseGet(() -> ensureCategory("Часы", "jewelry-watches", jewelryRoot, 1));
        moveCategoryIfNeeded(watchesSub, jewelryRoot, 1, "Часы");

        ensureCategory("Кольца", "jewelry-rings", jewelryRoot, 2);
        ensureCategory("Браслеты", "jewelry-bracelets", jewelryRoot, 3);
        ensureCategory("Серьги", "jewelry-earrings", jewelryRoot, 4);
        ensureCategory("Цепочки", "jewelry-chains", jewelryRoot, 5);
        ensureCategory("Кулоны", "jewelry-pendants", jewelryRoot, 6);
    }

    private Category ensureCategory(String name, String slug, Category parent, int sortOrder) {
        Category category = categoryRepository.findBySlug(slug).orElseGet(() -> Category.builder()
                .name(name)
                .slug(slug)
                .build());

        boolean changed = false;
        if (!name.equals(category.getName())) {
            category.setName(name);
            changed = true;
        }
        Long currentParentId = category.getParent() != null ? category.getParent().getId() : null;
        Long targetParentId = parent != null ? parent.getId() : null;
        if ((currentParentId == null && targetParentId != null) || (currentParentId != null && !currentParentId.equals(targetParentId))) {
            category.setParent(parent);
            changed = true;
        }
        if (category.getSortOrder() != sortOrder) {
            category.setSortOrder(sortOrder);
            changed = true;
        }

        if (category.getId() == null || changed) {
            category = categoryRepository.save(category);
        }
        return category;
    }

    private java.util.Optional<Category> findFirstCategoryByHints(String... hints) {
        List<Category> all = categoryRepository.findAll();
        return all.stream().filter(category -> {
            String name = category.getName() == null ? "" : category.getName().toLowerCase();
            String slug = category.getSlug() == null ? "" : category.getSlug().toLowerCase();
            for (String hint : hints) {
                String h = hint.toLowerCase();
                if (name.contains(h) || slug.contains(h)) return true;
            }
            return false;
        }).findFirst();
    }

    private void moveCategoryIfNeeded(Category category, Category targetParent, int sortOrder, String targetName) {
        Long currentParentId = category.getParent() != null ? category.getParent().getId() : null;
        boolean parentChanged = !java.util.Objects.equals(currentParentId, targetParent.getId());
        boolean sortChanged = category.getSortOrder() != sortOrder;
        boolean nameChanged = targetName != null && !targetName.isBlank() && !targetName.equals(category.getName());
        if (parentChanged || sortChanged || nameChanged) {
            category.setParent(targetParent);
            category.setSortOrder(sortOrder);
            if (nameChanged) {
                category.setName(targetName);
            }
            categoryRepository.save(category);
        }
    }

    private User ensureDefaultUsers() {
        Store defaultStore = storeRepository.findByActiveTrueOrderBySortOrderAscIdAsc().stream().findFirst().orElse(null);
        User admin = userRepository.findByEmail("admin@lombard.ru")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin@lombard.ru")
                        .passwordHash(passwordEncoder.encode("password"))
                        .fullName("Администратор")
                        .role(User.Role.ADMIN)
                        .blocked(false)
                        .build()));

        User manager = userRepository.findByEmail("manager@lombard.ru")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("manager@lombard.ru")
                        .passwordHash(passwordEncoder.encode("password"))
                        .fullName("Менеджер")
                        .role(User.Role.MANAGER)
                        .blocked(false)
                        .build()));
        if (manager.getRole() == User.Role.MANAGER && manager.getStore() == null && defaultStore != null) {
            manager.setStore(defaultStore);
            userRepository.save(manager);
        }

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

        List<Store> stores = storeRepository.findByActiveTrueOrderBySortOrderAscIdAsc();
        if (stores.isEmpty()) {
            return;
        }
        for (Category category : leafCategories) {
            long existing = productRepository.countByCategoryId(category.getId());
            for (int i = (int) existing + 1; i <= 2; i++) {
                productRepository.save(buildDemoProduct(category, author, i, stores));
            }
        }
    }

    private Product buildDemoProduct(Category category, User author, int index, List<Store> stores) {
        String slug = nextAvailableSlug(category.getSlug() + "-demo-" + index);
        Store store = stores.get((int) ((category.getId() + index) % stores.size()));
        return Product.builder()
                .category(category)
                .store(store)
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
