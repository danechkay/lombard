package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lombard.dto.ValuationRequestCreateResponseDto;
import ru.lombard.dto.ValuationRequestRowDto;
import ru.lombard.dto.ValuationRequestDto;
import ru.lombard.dto.ValuationResultDto;
import ru.lombard.dto.ValuationSetPriceRequestDto;
import ru.lombard.entity.Category;
import ru.lombard.entity.Product;
import ru.lombard.entity.User;
import ru.lombard.entity.ValuationRequest;
import ru.lombard.repository.CategoryRepository;
import ru.lombard.repository.ValuationRequestRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ValuationRequestService {

    private final ValuationRequestRepository valuationRequestRepository;
    private final ValuationService valuationService;
    private final CategoryRepository categoryRepository;

    // Важно: не держим внешний @Transactional, чтобы ошибка/исключение из расчёта
    // не помечало транзакцию на rollback-only и не приводило к 500.
    public ValuationRequestCreateResponseDto createRequest(User user, ValuationRequestDto requestDto) {
        requestDto.validate();

        // Ориентир по аналогам (не критично). Даже если расчёт упадёт,
        // заявка должна успешно сохраниться, и менеджер назначит цену.
        ValuationResultDto analog;
        try {
            analog = valuationService.calculate(requestDto);
        } catch (Throwable ex) {
            String conditionRaw = requestDto.getCondition() == null ? "USED" : requestDto.getCondition().trim().toUpperCase();
            analog = ValuationResultDto.builder()
                    .estimatedPrice(null)
                    .matchedCount(0L)
                    .condition(conditionRaw)
                    .yearAdjusted(false)
                    .requestYear(requestDto.getYear())
                    .averageYear(null)
                    .yearAdjustmentPercent(null)
                    .note("Не удалось автоматически рассчитать ориентир. Менеджер назначит цену после проверки.")
                    .build();
        }

        Category category = null;
        if (requestDto.getCategoryId() != null && requestDto.getCategoryId() > 0) {
            category = categoryRepository.findById(requestDto.getCategoryId()).orElse(null);
        }

        Product.Condition condition = Product.Condition.valueOf(requestDto.getCondition().trim().toUpperCase());

        ValuationRequest vr = ValuationRequest.builder()
                .user(user)
                .category(category)
                .modelName(requestDto.normalizedModelName())
                .description(requestDto.getDescription())
                .condition(condition)
                .year(requestDto.getYear())
                .status(ValuationRequest.Status.NEW)
                .analogEstimatedPrice(analog.getEstimatedPrice())
                .analogMatchedCount(analog.getMatchedCount() == null ? 0L : analog.getMatchedCount())
                .build();

        ValuationRequest saved;
        try {
            saved = valuationRequestRepository.save(vr);
        } catch (RuntimeException ex) {
            // Чтобы фронт не видел "Internal Server Error" без текста:
            // обычно такое бывает, если миграции БД не применились (новая колонка/тип).
            throw new IllegalStateException("Не удалось сохранить заявку на оценку. Проверьте миграции БД и перезапустите сервер.");
        }

        return ValuationRequestCreateResponseDto.builder()
                .id(saved.getId())
                .status(saved.getStatus().name())
                .analogEstimatedPrice(saved.getAnalogEstimatedPrice())
                .analogMatchedCount(saved.getAnalogMatchedCount())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ValuationRequestRowDto> findByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return valuationRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toRowDto);
    }

    @Transactional(readOnly = true)
    public Page<ValuationRequestRowDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return valuationRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toRowDto);
    }

    @Transactional
    public ValuationRequestRowDto setPrice(User manager, Long id, ValuationSetPriceRequestDto request) {
        if (request == null || request.getPrice() == null) {
            throw new IllegalArgumentException("Укажите цену");
        }

        ValuationRequest vr = valuationRequestRepository.findWithDetailsById(id).orElseThrow();

        if (vr.getStatus() != ValuationRequest.Status.NEW) {
            throw new IllegalStateException("Цена уже назначена или заявка обработана");
        }

        BigDecimal price = request.getPrice();
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }

        vr.setManager(manager);
        vr.setManagerPrice(price.setScale(2, RoundingMode.HALF_UP));
        vr.setManagerNote(request.getNote());
        vr.setStatus(ValuationRequest.Status.PRICED);

        ValuationRequest saved = valuationRequestRepository.save(vr);
        return toRowDto(saved);
    }

    private ValuationRequestRowDto toRowDto(ValuationRequest vr) {
        return ValuationRequestRowDto.builder()
                .id(vr.getId())
                .modelName(vr.getModelName())
                .description(vr.getDescription())
                .condition(vr.getCondition())
                .year(vr.getYear())
                .status(vr.getStatus())
                .analogEstimatedPrice(vr.getAnalogEstimatedPrice())
                .analogMatchedCount(vr.getAnalogMatchedCount())
                .managerPrice(vr.getManagerPrice())
                .managerNote(vr.getManagerNote())
                .createdAt(vr.getCreatedAt())
                .updatedAt(vr.getUpdatedAt())
                .build();
    }
}

