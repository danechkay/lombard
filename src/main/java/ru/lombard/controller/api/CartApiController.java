package ru.lombard.controller.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.lombard.dto.CartItemDto;
import ru.lombard.service.CartService;
import ru.lombard.service.CurrentUserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public Map<String, Object> cart() {
        Long userId = requireUserId();
        List<CartItemDto> items = cartService.getCart(userId);
        BigDecimal total = cartService.getCartTotal(userId);
        return Map.of(
                "items", items,
                "count", cartService.getCartCount(userId),
                "total", total
        );
    }

    @PostMapping("/add")
    public Map<String, String> add(@RequestBody AddToCartRequest request) {
        Long userId = requireUserId();
        int quantity = request.getQuantity() <= 0 ? 1 : request.getQuantity();
        cartService.addToCart(userId, request.getProductId(), quantity);
        return Map.of("message", "Товар добавлен в корзину.");
    }

    @PostMapping("/update")
    public Map<String, String> update(@RequestBody UpdateCartRequest request) {
        Long userId = requireUserId();
        cartService.updateQuantity(userId, request.getCartItemId(), request.getQuantity());
        return Map.of("message", "Количество обновлено.");
    }

    @PostMapping("/remove")
    public Map<String, String> remove(@RequestBody RemoveFromCartRequest request) {
        Long userId = requireUserId();
        cartService.removeFromCart(userId, request.getProductId());
        return Map.of("message", "Товар удален из корзины.");
    }

    private Long requireUserId() {
        Long userId = currentUserService.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Нужна авторизация");
        }
        return userId;
    }

    @Data
    public static class AddToCartRequest {
        private Long productId;
        private int quantity = 1;
    }

    @Data
    public static class UpdateCartRequest {
        private Long cartItemId;
        private int quantity;
    }

    @Data
    public static class RemoveFromCartRequest {
        private Long productId;
    }
}
