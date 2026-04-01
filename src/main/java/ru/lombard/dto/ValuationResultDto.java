package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ValuationResultDto {
    private BigDecimal estimatedPrice;
    private Long matchedCount;
    private String condition;

    // Упрощенная корректировка по году (если указан год и в базе есть год у достаточного количества товаров).
    private boolean yearAdjusted;
    private Integer requestYear;
    private Integer averageYear;
    private BigDecimal yearAdjustmentPercent;

    private String note;
}

