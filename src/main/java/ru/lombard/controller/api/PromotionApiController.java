package ru.lombard.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lombard.dto.PromotionDto;
import ru.lombard.service.PromotionService;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionApiController {

    private final PromotionService promotionService;

    @GetMapping
    public List<PromotionDto> promotions() {
        return promotionService.findActive();
    }
}
