package ru.lombard.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lombard.entity.User;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.UserService;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public Map<String, ?> me() {
        return currentUserService.getCurrentUser()
                .map(user -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("authenticated", true);
                    payload.put("id", user.getId());
                    payload.put("email", user.getEmail());
                    payload.put("fullName", user.getFullName());
                    payload.put("phone", user.getPhone() == null ? "" : user.getPhone());
                    payload.put("role", user.getRole() == null ? "USER" : user.getRole().name());
                    payload.put("storeId", user.getStore() != null ? user.getStore().getId() : null);
                    payload.put("storeName", user.getStore() != null ? user.getStore().getName() : null);
                    return payload;
                })
                .orElseGet(() -> Map.of("authenticated", false));
    }

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.register(request.getEmail(), request.getPassword(), request.getFullName(), request.getPhone());
            return Map.of("message", "Регистрация успешна. Теперь войдите в систему.");
        } catch (IllegalArgumentException e) {
            return Map.of("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return Map.of("error", "Не удалось завершить регистрацию. Проверьте корректность данных.");
        }
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("message", "Для выхода отправьте POST на /logout.");
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Укажите email")
        @Email
        private String email;

        @NotBlank(message = "Укажите пароль")
        @Size(min = 5, message = "Пароль не менее 5 символов")
        private String password;

        @NotBlank(message = "Укажите имя")
        private String fullName;

        @Size(max = 20, message = "Телефон не должен быть длиннее 20 символов")
        private String phone;
    }
}
