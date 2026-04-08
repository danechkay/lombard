package ru.lombard.controller.api.admin;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.lombard.dto.OrderDto;
import ru.lombard.entity.Order;
import ru.lombard.entity.User;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.OrderService;

import java.util.Map;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AdminOrderApiController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public Object list(@RequestParam(defaultValue = "0") int page) {
        return orderService.findAllOrders(requireActor(), page, 20).map(orderService::toDto);
    }

    @GetMapping("/{id}")
    public OrderDto view(@PathVariable Long id) {
        return orderService.findDtoByIdForActor(requireActor(), id);
    }

    @PostMapping("/{id}/status")
    public Map<String, String> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        orderService.updateStatusForActor(
                requireActor(),
                id,
                parseEnum(Order.OrderStatus.class, request.getStatus(), "Некорректный статус заказа")
        );
        return Map.of("message", "Статус заказа обновлен");
    }

    @Data
    public static class StatusRequest {
        private String status;
    }

    private User requireActor() {
        return currentUserService.getCurrentUser().orElseThrow();
    }

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
}
