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
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .productName(ci.getProduct().getName())
                    .price(ci.getProduct().getPrice())
                    .quantity(ci.getQuantity())
                    .build();
            order.getItems().add(oi);
            total = total.add(ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
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
        order.setOrderStatus(status);
        orderRepository.save(order);
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
