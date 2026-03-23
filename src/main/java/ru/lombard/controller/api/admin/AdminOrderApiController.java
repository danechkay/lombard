package ru.lombard.controller.api.admin;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.lombard.dto.OrderDto;
import ru.lombard.entity.Order;
import ru.lombard.service.OrderService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderApiController {

    private final OrderService orderService;

    @GetMapping
    public Object list(@RequestParam(defaultValue = "0") int page) {
        return orderService.findAllOrders(page, 20).map(orderService::toDto);
    }

    @GetMapping("/{id}")
    public OrderDto view(@PathVariable Long id) {
        return orderService.findDtoById(id);
    }

    @PostMapping("/{id}/status")
    public Map<String, String> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        orderService.updateStatus(id, Order.OrderStatus.valueOf(request.getStatus()));
        return Map.of("message", "Статус заказа обновлен");
    }

    @Data
    public static class StatusRequest {
        private String status;
    }
}
