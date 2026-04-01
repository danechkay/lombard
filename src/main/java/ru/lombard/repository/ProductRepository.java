package ru.lombard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.lombard.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "images"})
    Optional<Product> findBySlug(String slug);

    @EntityGraph(attributePaths = {"category", "images"})
    Optional<Product> findById(Long id);

    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "images"})
    @Query("SELECT p FROM Product p")
    Page<Product> findAllWithCategory(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.status = :status " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:condition IS NULL OR p.condition = :condition) " +
           "AND (:search IS NULL OR LOWER(p.name) LIKE CONCAT('%', CAST(:search AS string), '%'))")
    Page<Product> findPublishedWithFilters(
        @Param("status") Product.ProductStatus status,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("condition") Product.Condition condition,
        @Param("search") String search,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.status = :status " +
           "AND ((:categoryIds) IS NULL OR p.category.id IN :categoryIds) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:condition IS NULL OR p.condition = :condition) " +
           "AND (:search IS NULL OR LOWER(p.name) LIKE CONCAT('%', CAST(:search AS string), '%'))")
    Page<Product> findPublishedWithCategoryTree(
        @Param("status") Product.ProductStatus status,
        @Param("categoryIds") List<Long> categoryIds,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("condition") Product.Condition condition,
        @Param("search") String search,
        Pageable pageable
    );

    long countByCategoryId(Long categoryId);
}
