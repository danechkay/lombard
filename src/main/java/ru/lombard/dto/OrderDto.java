package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;
import ru.lombard.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private Long storeId;
    private String storeName;
    private String storeAddress;
    private Order.OrderStatus orderStatus;
    private boolean reserved;
    private String reservedFor;
    private String pickupCode;
    private BigDecimal totalAmount;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemDto> items;
}
