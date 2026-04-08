package ru.lombard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.lombard.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user", "store"})
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "store"})
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "store"})
    Page<Order> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "store"})
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN o.items oi
            LEFT JOIN oi.product p
            WHERE o.store.id = :storeId OR p.store.id = :storeId
            ORDER BY o.createdAt DESC
            """)
    Page<Order> findManagerScopedOrders(@Param("storeId") Long storeId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "store", "items"})
    Optional<Order> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"user", "store", "items"})
    Optional<Order> findWithDetailsByIdAndStoreId(Long id, Long storeId);

    @EntityGraph(attributePaths = {"user", "store", "items"})
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN o.items oi
            LEFT JOIN oi.product p
            WHERE o.id = :id AND (o.store.id = :storeId OR p.store.id = :storeId)
            """)
    Optional<Order> findManagerScopedWithDetailsById(@Param("id") Long id, @Param("storeId") Long storeId);

    Optional<Order> findByIdAndStoreId(Long id, Long storeId);

    @Query("""
            SELECT o
            FROM Order o
            LEFT JOIN o.items oi
            LEFT JOIN oi.product p
            WHERE o.id = :id AND (o.store.id = :storeId OR p.store.id = :storeId)
            """)
    Optional<Order> findManagerScopedById(@Param("id") Long id, @Param("storeId") Long storeId);
}
