package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromotionDto {
    private Long id;
    private String title;
    private String subtitle;
    private String description;
    private String buttonText;
    private String imageUrl;
    private boolean active;
    private int sortOrder;
}
