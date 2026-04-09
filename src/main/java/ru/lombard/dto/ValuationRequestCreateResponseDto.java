package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ValuationRequestCreateResponseDto {
    private Long id;
    private String status;
    private BigDecimal analogEstimatedPrice;
    private Long analogMatchedCount;
}

