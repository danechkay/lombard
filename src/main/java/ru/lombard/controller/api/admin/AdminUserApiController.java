package ru.lombard.controller.api.admin;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.lombard.entity.User;
import ru.lombard.repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserApiController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserRow> list() {
        return userRepository.findAll().stream()
                .map(user -> new UserRow(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getPhone() == null ? "" : user.getPhone(),
                        user.getRole().name(),
                        user.isBlocked()
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
        user.setRole(User.Role.valueOf(request.getRole()));
        userRepository.save(user);
        return Map.of("message", "Роль обновлена");
    }

    @Data
    public static class RoleRequest {
        private String role;
    }

    public record UserRow(
            Long id,
            String email,
            String fullName,
            String phone,
            String role,
            boolean blocked
    ) {}
}
