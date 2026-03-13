package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemDto {
    private Long id;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subtotal;
}
