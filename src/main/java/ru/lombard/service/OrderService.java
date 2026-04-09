package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lombard.dto.OrderDto;
import ru.lombard.dto.OrderItemDto;
import ru.lombard.entity.*;
import ru.lombard.repository.CartItemRepository;
import ru.lombard.repository.OrderRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final String PICKUP_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int PICKUP_CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Order createOrder(User user, String comment) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedAtDesc(user.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Корзина пуста");
        }
        Store orderStore = cartItems.get(0).getProduct().getStore();
        if (orderStore == null || orderStore.getId() == null) {
            throw new IllegalStateException("У товара не указан магазин");
        }
        for (CartItem ci : cartItems) {
            Store store = ci.getProduct().getStore();
            if (store == null || !orderStore.getId().equals(store.getId())) {
                throw new IllegalStateException("В одном заказе могут быть только товары одного магазина");
            }
        }
        BigDecimal total = BigDecimal.ZERO;
        Order order = Order.builder()
                .user(user)
                .store(orderStore)
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
    public Page<Order> findAllOrders(User actor, int page, int size) {
        if (actor.getRole() == User.Role.ADMIN) {
            return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        Long storeId = managerStoreId(actor);
        return orderRepository.findManagerScopedOrders(storeId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public OrderDto findDtoByIdForActor(User actor, Long id) {
        Order order;
        if (actor.getRole() == User.Role.ADMIN) {
            order = orderRepository.findWithDetailsById(id).orElseThrow();
        } else {
            order = orderRepository.findManagerScopedWithDetailsById(id, managerStoreId(actor)).orElseThrow();
        }
        return toDto(order);
    }

    @Transactional
    public void updateStatusForActor(User actor, Long orderId, Order.OrderStatus status) {
        Order order;
        if (actor.getRole() == User.Role.ADMIN) {
            order = orderRepository.findById(orderId).orElseThrow();
        } else {
            order = orderRepository.findManagerScopedById(orderId, managerStoreId(actor)).orElseThrow();
        }
        if (!isTransitionAllowed(order.getOrderStatus(), status)) {
            throw new IllegalArgumentException("Недопустимый переход статуса заказа");
        }
        order.setOrderStatus(status);
        if (status == Order.OrderStatus.PAID && (order.getPickupCode() == null || order.getPickupCode().isBlank())) {
            order.setPickupCode(generatePickupCode());
        }
        orderRepository.save(order);
    }

    /**
     * Псевдо-оплата для прототипа: переводит заказ владельца из NEW в PAID.
     * Настоящую интеграцию с платежной системой добавим позже.
     */
    @Transactional
    public Order mockPay(User currentUser, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // Защита от попыток оплатить чужой заказ.
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Нельзя оплатить заказ другого пользователя");
        }

        if (!isTransitionAllowed(order.getOrderStatus(), Order.OrderStatus.PAID)) {
            throw new IllegalStateException("Оплата недоступна для текущего статуса заказа");
        }

        order.setOrderStatus(Order.OrderStatus.PAID);
        if (order.getPickupCode() == null || order.getPickupCode().isBlank()) {
            order.setPickupCode(generatePickupCode());
        }
        orderRepository.save(order);
        return order;
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
        try {
            Hibernate.initialize(o.getItems());
        } catch (LazyInitializationException ignored) {
            // Для списков заказов достаточно базовой информации, даже если items ленивые.
        }
        List<OrderItemDto> items = List.of();
        if (Hibernate.isInitialized(o.getItems())) {
            items = o.getItems().stream()
                    .map(i -> OrderItemDto.builder()
                            .id(i.getId())
                            .productName(i.getProductName())
                            .price(i.getPrice())
                            .quantity(i.getQuantity())
                            .subtotal(i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                            .build())
                    .collect(Collectors.toList());
        }
        return OrderDto.builder()
                .id(o.getId())
                .userId(o.getUser().getId())
                .userEmail(o.getUser().getEmail())
                .userFullName(o.getUser().getFullName())
                .storeId(o.getStore() != null ? o.getStore().getId() : null)
                .storeName(o.getStore() != null ? o.getStore().getName() : null)
                .storeAddress(o.getStore() != null ? o.getStore().getAddress() : null)
                .orderStatus(o.getOrderStatus())
                .reserved(isReservedStatus(o.getOrderStatus()))
                .reservedFor(o.getUser().getFullName() + " (" + o.getUser().getEmail() + ")")
                .pickupCode(o.getPickupCode())
                .totalAmount(o.getTotalAmount())
                .comment(o.getComment())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .items(items)
                .build();
    }

    private boolean isReservedStatus(Order.OrderStatus status) {
        return status == Order.OrderStatus.PAID || status == Order.OrderStatus.SHIPPED;
    }

    private Long managerStoreId(User actor) {
        if (actor.getStore() == null || actor.getStore().getId() == null) {
            throw new IllegalStateException("Менеджер не привязан к магазину");
        }
        return actor.getStore().getId();
    }

    private String generatePickupCode() {
        StringBuilder code = new StringBuilder(PICKUP_CODE_LENGTH);
        for (int i = 0; i < PICKUP_CODE_LENGTH; i++) {
            int idx = RANDOM.nextInt(PICKUP_CODE_ALPHABET.length());
            code.append(PICKUP_CODE_ALPHABET.charAt(idx));
        }
        return code.toString();
    }
}
