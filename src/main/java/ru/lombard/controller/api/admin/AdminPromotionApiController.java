package ru.lombard.controller.api.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.lombard.dto.PromotionDto;
import ru.lombard.service.PromotionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionApiController {

    private final PromotionService promotionService;

    @GetMapping
    public List<PromotionDto> list() {
        return promotionService.findAll();
    }

    @PostMapping
    public PromotionDto create(@RequestBody PromotionDto dto) {
        return promotionService.create(dto);
    }

    @PostMapping("/{id}")
    public PromotionDto update(@PathVariable Long id, @RequestBody PromotionDto dto) {
        return promotionService.update(id, dto);
    }

    @PostMapping("/{id}/delete")
    public Map<String, String> delete(@PathVariable Long id) {
        promotionService.delete(id);
        return Map.of("message", "Акция удалена");
    }
}
