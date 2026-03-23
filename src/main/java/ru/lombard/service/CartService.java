package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lombard.dto.CartItemDto;
import ru.lombard.entity.CartItem;
import ru.lombard.entity.Product;
import ru.lombard.entity.User;
import ru.lombard.repository.CartItemRepository;
import ru.lombard.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public List<CartItemDto> getCart(Long userId) {
        return cartItemRepository.findByUserIdOrderByAddedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public int getCartCount(Long userId) {
        return (int) cartItemRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(Long userId) {
        return getCart(userId).stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElseThrow();
        if (product.getStatus() != Product.ProductStatus.PUBLISHED) {
            throw new IllegalArgumentException("Товар недоступен для заказа");
        }
        cartItemRepository.findByUserIdAndProductId(userId, productId).ifPresentOrElse(
                item -> {
                    item.setQuantity(item.getQuantity() + quantity);
                    cartItemRepository.save(item);
                },
                () -> {
                    CartItem item = CartItem.builder()
                            .user(User.builder().id(userId).build())
                            .product(product)
                            .quantity(quantity)
                            .build();
                    cartItemRepository.save(item);
                }
        );
    }

    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow();
        if (!item.getUser().getId().equals(userId)) throw new IllegalArgumentException("Не ваш элемент корзины");
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemDto toDto(CartItem item) {
        String mainUrl = item.getProduct().getImages().stream()
                .filter(pi -> pi.isMain()).map(pi -> pi.getImageUrl()).findFirst()
                .orElseGet(() -> item.getProduct().getImages().isEmpty() ? null : item.getProduct().getImages().get(0).getImageUrl());
        BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSlug(item.getProduct().getSlug())
                .mainImageUrl(mainUrl)
                .price(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
