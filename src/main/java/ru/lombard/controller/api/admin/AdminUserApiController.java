package ru.lombard.controller.api.admin;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.lombard.entity.Store;
import ru.lombard.entity.User;
import ru.lombard.repository.StoreRepository;
import ru.lombard.repository.UserRepository;

import java.util.List;
import java.util.Map;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserApiController {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @GetMapping
    public List<UserRow> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone
    ) {
        return userRepository.searchForAdmin(toLikePattern(name), toLikePattern(email), toLikePattern(phone)).stream()
                .map(user -> new UserRow(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getPhone() == null ? "" : user.getPhone(),
                        user.getRole().name(),
                        user.isBlocked(),
                        user.getStore() != null ? user.getStore().getId() : null,
                        user.getStore() != null ? user.getStore().getName() : null
                ))
                .toList();
    }

    @PostMapping("/{id}/block")
    public Map<String, String> block(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBlocked(true);
        userRepository.save(user);
        return Map.of("message", "Пользователь заблокирован");
    }

    @PostMapping("/{id}/unblock")
    public Map<String, String> unblock(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBlocked(false);
        userRepository.save(user);
        return Map.of("message", "Пользователь разблокирован");
    }

    @PostMapping("/{id}/role")
    public Map<String, String> changeRole(@PathVariable Long id, @RequestBody RoleRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        User.Role nextRole = parseEnum(User.Role.class, request.getRole(), "Некорректная роль");
        user.setRole(nextRole);
        if (nextRole == User.Role.MANAGER) {
            Long storeId = request.getStoreId();
            if (storeId == null) {
                throw new ResponseStatusException(BAD_REQUEST, "Для менеджера нужно выбрать магазин");
            }
            Store store = storeRepository.findById(storeId)
                    .filter(Store::isActive)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Некорректный магазин"));
            user.setStore(store);
        } else {
            user.setStore(null);
        }
        userRepository.save(user);
        return Map.of("message", "Роль обновлена");
    }

    @Data
    public static class RoleRequest {
        private String role;
        private Long storeId;
    }

    public record UserRow(
            Long id,
            String email,
            String fullName,
            String phone,
            String role,
            boolean blocked,
            Long storeId,
            String storeName
    ) {}

    private static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, message);
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, message);
        }
    }

    private static String toLikePattern(String value) {
        if (value == null) return null;
        String trimmed = value.trim().toLowerCase();
        return trimmed.isEmpty() ? null : "%" + trimmed + "%";
    }
}
