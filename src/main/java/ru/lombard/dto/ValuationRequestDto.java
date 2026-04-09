package ru.lombard.dto;

import lombok.Data;

import java.util.Objects;

@Data
public class ValuationRequestDto {
    private Long categoryId; // опционально
    private String modelName; // обязательно: название модели/товара
    private String description; // опционально: что не так с товаром
    private String condition; // NEW / USED
    private Integer year; // опционально

    public void validate() {
        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("Укажите название модели/товара");
        }
        if (condition == null || condition.isBlank()) {
            throw new IllegalArgumentException("Укажите состояние товара");
        }
    }

    public String normalizedModelName() {
        return modelName == null ? null : modelName.trim();
    }

    public boolean hasYear() {
        return year != null && year > 0;
    }
}

