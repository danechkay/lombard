package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;
import ru.lombard.entity.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Product.Condition condition;
    private BigDecimal price;
    private int quantity;
    private Product.ProductStatus status;
    private Integer year;
    private int viewsCount;
    private Long categoryId;
    private String categoryName;
    private Instant createdAt;
    private Instant publishedAt;
    private List<String> imageUrls;
    private String mainImageUrl;
}
