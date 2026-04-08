package ru.lombard.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.lombard.entity.Loan;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @EntityGraph(attributePaths = {"user", "manager"})
    List<Loan> findByUserIdOrderByIssueDateDesc(Long userId);
}
