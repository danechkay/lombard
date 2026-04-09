package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;
import ru.lombard.entity.Product;
import ru.lombard.entity.ValuationRequest;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ValuationRequestRowDto {
    private Long id;
    private String modelName;
    private String description;
    private Product.Condition condition;
    private Integer year;
    private ValuationRequest.Status status;

    private BigDecimal analogEstimatedPrice;
    private Long analogMatchedCount;

    private BigDecimal managerPrice;
    private String managerNote;

    private Instant createdAt;
    private Instant updatedAt;
}

