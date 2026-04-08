package ru.lombard.controller.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.lombard.entity.Order;
import ru.lombard.entity.User;
import ru.lombard.service.CartService;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.OrderService;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;
    private final CartService cartService;

    @GetMapping("/checkout")
    public Map<String, Object> checkout() {
        User user = requireUser();
        var cartItems = cartService.getCart(user.getId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Корзина пуста");
        }
        return Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "phone", user.getPhone() == null ? "" : user.getPhone()
                ),
                "cartItems", cartItems,
                "total", cartService.getCartTotal(user.getId())
        );
    }

    @PostMapping("/place")
    public Map<String, Object> placeOrder(@RequestBody PlaceOrderRequest request) {
        User user = requireUser();
        try {
            Order order = orderService.createOrder(user, request.getComment());
            return Map.of(
                    "message", "Заказ оформлен",
                    "orderId", order.getId()
            );
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Псевдо-оплата для прототипа: переводит заказ владельца из NEW в PAID.
     * Реальную интеграцию с платежной системой добавим позже.
     */
    @PostMapping("/{id}/mock-pay")
    public Map<String, String> mockPay(@PathVariable Long id) {
        User user = requireUser();
        try {
            var order = orderService.mockPay(user, id);
            return Map.of(
                    "message", "Оплата успешна (тестовый режим)",
                    "orderStatus", order.getOrderStatus().name(),
                    "pickupCode", order.getPickupCode() == null ? "" : order.getPickupCode()
            );
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }
    }

    private User requireUser() {
        return currentUserService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Нужна авторизация"));
    }

    @Data
    public static class PlaceOrderRequest {
        private String comment;
    }
}
