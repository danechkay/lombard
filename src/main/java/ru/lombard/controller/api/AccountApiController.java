package ru.lombard.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.lombard.entity.User;
import ru.lombard.repository.UserRepository;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.LoanService;
import ru.lombard.service.OrderService;
import ru.lombard.service.ValuationRequestService;

import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountApiController {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final ValuationRequestService valuationRequestService;
    private final LoanService loanService;

    @GetMapping
    public Map<String, Object> account() {
        User user = requireUser();
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "phone", user.getPhone() == null ? "" : user.getPhone(),
                "role", user.getRole() == null ? "USER" : user.getRole().name()
        );
    }

    @GetMapping("/orders")
    public Object orders() {
        User user = requireUser();
        return orderService.findOrdersByUser(user.getId(), 50);
    }

    @GetMapping("/valuations")
    public Object valuations() {
        User user = requireUser();
        // Для простоты вернем первую страницу (можно расширить пагинацией позже).
        return valuationRequestService.findByUser(user.getId(), 0, 50);
    }

    @GetMapping("/loans")
    public Object loans() {
        User user = requireUser();
        return loanService.findByUser(user.getId());
    }

    @PostMapping("/phone")
    public Map<String, String> updatePhone(@RequestBody PhoneRequest request) {
        User user = requireUser();
        String phone = request != null && request.phone != null ? request.phone.trim() : "";
        user.setPhone(phone);
        userRepository.save(user);
        return Map.of("message", "Телефон обновлен");
    }

    public static class PhoneRequest {
        public String phone;
    }

    private User requireUser() {
        return currentUserService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Нужна авторизация"));
    }
}
