package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private int sortOrder;
}
