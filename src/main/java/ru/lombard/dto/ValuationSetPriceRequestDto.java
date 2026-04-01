package ru.lombard.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ValuationSetPriceRequestDto {
    private BigDecimal price;
    private String note;
}

