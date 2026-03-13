package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String mainImageUrl;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subtotal;
}
