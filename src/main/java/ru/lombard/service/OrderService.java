package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lombard.dto.OrderDto;
import ru.lombard.dto.OrderItemDto;
import ru.lombard.entity.*;
import ru.lombard.repository.CartItemRepository;
import ru.lombard.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Order createOrder(User user, String comment) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedAtDesc(user.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Корзина пуста");
        }
        BigDecimal total = BigDecimal.ZERO;
        Order order = Order.builder()
                .user(user)
                .orderStatus(Order.OrderStatus.NEW)
                .totalAmount(BigDecimal.ZERO)
                .comment(comment)
                .build();
        order = orderRepository.save(order);
        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            if (product.getStatus() != Product.ProductStatus.PUBLISHED) {
                throw new IllegalStateException("Один из товаров больше недоступен");
            }
            if (ci.getQuantity() > product.getQuantity()) {
                throw new IllegalStateException("Недостаточно товара на складе");
            }
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(ci.getQuantity())
                    .build();
            order.getItems().add(oi);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            product.setQuantity(product.getQuantity() - ci.getQuantity());
        }
        order.setTotalAmount(total);
        orderRepository.save(order);
        cartItemRepository.deleteByUserId(user.getId());
        return order;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findOrdersByUser(Long userId, int limit) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit, Sort.by("createdAt").descending())).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Order> findAllOrders(int page, int size) {
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public OrderDto findDtoById(Long id) {
        Order order = orderRepository.findWithDetailsById(id).orElseThrow();
        return toDto(order);
    }

    @Transactional
    public void updateStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!isTransitionAllowed(order.getOrderStatus(), status)) {
            throw new IllegalArgumentException("Недопустимый переход статуса заказа");
        }
        order.setOrderStatus(status);
        orderRepository.save(order);
    }

    /**
     * Псевдо-оплата для прототипа: переводит заказ владельца из NEW в PAID.
     * Настоящую интеграцию с платежной системой добавим позже.
     */
    @Transactional
    public Order.OrderStatus mockPay(User currentUser, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // Защита от попыток оплатить чужой заказ.
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Нельзя оплатить заказ другого пользователя");
        }

        if (!isTransitionAllowed(order.getOrderStatus(), Order.OrderStatus.PAID)) {
            throw new IllegalStateException("Оплата недоступна для текущего статуса заказа");
        }

        order.setOrderStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);
        return order.getOrderStatus();
    }

    private boolean isTransitionAllowed(Order.OrderStatus from, Order.OrderStatus to) {
        if (from == to) return true;
        return switch (from) {
            case NEW -> to == Order.OrderStatus.PAID || to == Order.OrderStatus.CANCELLED;
            case PAID -> to == Order.OrderStatus.SHIPPED || to == Order.OrderStatus.CANCELLED;
            case SHIPPED -> to == Order.OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    public OrderDto toDto(Order o) {
        List<OrderItemDto> items = o.getItems().stream()
                .map(i -> OrderItemDto.builder()
                        .id(i.getId())
                        .productName(i.getProductName())
                        .price(i.getPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .build())
                .collect(Collectors.toList());
        return OrderDto.builder()
                .id(o.getId())
                .userId(o.getUser().getId())
                .userEmail(o.getUser().getEmail())
                .userFullName(o.getUser().getFullName())
                .orderStatus(o.getOrderStatus())
                .totalAmount(o.getTotalAmount())
                .comment(o.getComment())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .items(items)
                .build();
    }
}
