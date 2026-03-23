package ru.lombard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.lombard.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items"})
    Optional<Order> findWithDetailsById(Long id);
}
