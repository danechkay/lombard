package ru.lombard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.lombard.entity.ValuationRequest;

import java.util.Optional;

public interface ValuationRequestRepository extends JpaRepository<ValuationRequest, Long> {

    @EntityGraph(attributePaths = {"user", "manager", "category"})
    Page<ValuationRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "manager", "category"})
    Page<ValuationRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "manager", "category"})
    Optional<ValuationRequest> findWithDetailsById(Long id);
}

