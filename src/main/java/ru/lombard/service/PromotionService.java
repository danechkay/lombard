package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lombard.dto.PromotionDto;
import ru.lombard.entity.Promotion;
import ru.lombard.repository.PromotionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public List<PromotionDto> findActive() {
        return promotionRepository.findByActiveTrueOrderBySortOrderAscIdDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromotionDto> findAll() {
        return promotionRepository.findAllByOrderBySortOrderAscIdDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PromotionDto create(PromotionDto dto) {
        Promotion promotion = Promotion.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .description(dto.getDescription())
                .buttonText(dto.getButtonText())
                .imageUrl(dto.getImageUrl())
                .active(dto.isActive())
                .sortOrder(dto.getSortOrder())
                .build();
        return toDto(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionDto update(Long id, PromotionDto dto) {
        Promotion promotion = promotionRepository.findById(id).orElseThrow();
        promotion.setTitle(dto.getTitle());
        promotion.setSubtitle(dto.getSubtitle());
        promotion.setDescription(dto.getDescription());
        promotion.setButtonText(dto.getButtonText());
        promotion.setImageUrl(dto.getImageUrl());
        promotion.setActive(dto.isActive());
        promotion.setSortOrder(dto.getSortOrder());
        return toDto(promotionRepository.save(promotion));
    }

    @Transactional
    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }

    public PromotionDto toDto(Promotion p) {
        return PromotionDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .subtitle(p.getSubtitle())
                .description(p.getDescription())
                .buttonText(p.getButtonText())
                .imageUrl(p.getImageUrl())
                .active(p.isActive())
                .sortOrder(p.getSortOrder())
                .build();
    }
}
