package ru.lombard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreDto {
    private Long id;
    private String name;
    private String slug;
    private String address;
}
