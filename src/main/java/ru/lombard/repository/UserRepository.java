package ru.lombard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.lombard.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"store"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"store"})
    List<User> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"store"})
    @Query("""
            SELECT u
            FROM User u
            WHERE (:namePattern IS NULL OR LOWER(u.fullName) LIKE :namePattern)
              AND (:emailPattern IS NULL OR LOWER(u.email) LIKE :emailPattern)
              AND (:phonePattern IS NULL OR LOWER(COALESCE(u.phone, '')) LIKE :phonePattern)
            ORDER BY u.id ASC
            """)
    List<User> searchForAdmin(
            @Param("namePattern") String namePattern,
            @Param("emailPattern") String emailPattern,
            @Param("phonePattern") String phonePattern
    );

}
