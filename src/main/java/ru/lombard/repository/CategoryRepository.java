package ru.lombard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.lombard.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNullOrderBySortOrderAsc();

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
