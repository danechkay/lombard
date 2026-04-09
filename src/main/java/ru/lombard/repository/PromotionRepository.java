package ru.lombard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.lombard.entity.Promotion;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByActiveTrueOrderBySortOrderAscIdDesc();
    List<Promotion> findAllByOrderBySortOrderAscIdDesc();
}
