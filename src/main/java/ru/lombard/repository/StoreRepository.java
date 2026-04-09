package ru.lombard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.lombard.entity.Store;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByActiveTrueOrderBySortOrderAscIdAsc();
}
