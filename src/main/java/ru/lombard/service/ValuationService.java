package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import ru.lombard.dto.ValuationRequestDto;
import ru.lombard.dto.ValuationResultDto;
import ru.lombard.entity.Product;
import ru.lombard.repository.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ValuationService {

    private static final int MAX_MATCHES = 200;

    private final ProductRepository productRepository;

    // Важно: расчёт может падать на неконсистентных данных.
    // Делаем отдельную транзакцию, чтобы исключить ситуацию,
    // когда исключение из расчёта помечает внешний Transaction как rollback-only.
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public ValuationResultDto calculate(ValuationRequestDto request) {
        request.validate();

        Product.Condition condition;
        try {
            condition = Product.Condition.valueOf(request.getCondition().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Некорректное состояние. Используйте NEW или USED");
        }

        String search = request.normalizedModelName();
        if (search != null && search.isBlank()) search = null;

        Long categoryId = request.getCategoryId();
        if (categoryId != null && categoryId <= 0) categoryId = null;

        Integer year = request.getYear();
        if (year != null && year <= 0) year = null;

        Pageable pageableByPrice = PageRequest.of(0, MAX_MATCHES, Sort.by("price").ascending());

        Page<Product> page1 = productRepository.findPublishedWithFilters(
                Product.ProductStatus.PUBLISHED,
                categoryId,
                null,
                null,
                null,
                condition,
                search,
                pageableByPrice
        );

        List<Product> matches = page1.getContent();

        // Если по точному запросу мало/нет похожих товаров — расширяем поиск (без modelName).
        if (matches.size() < 3) {
            Page<Product> page2 = productRepository.findPublishedWithFilters(
                    Product.ProductStatus.PUBLISHED,
                    categoryId,
                    null,
                    null,
                    null,
                    condition,
                    null,
                    pageableByPrice
            );
            if (page2.getContent().size() > matches.size()) {
                matches = page2.getContent();
            }
        }

        if (matches.isEmpty()) {
            return ValuationResultDto.builder()
                    .estimatedPrice(null)
                    .matchedCount(0L)
                    .condition(condition.name())
                    .yearAdjusted(false)
                    .requestYear(year)
                    .averageYear(null)
                    .yearAdjustmentPercent(null)
                    .note("Похоже, нет опубликованных товаров с таким состоянием/названием. Попробуйте изменить параметры.")
                    .build();
        }

        List<BigDecimal> prices = matches.stream()
                .map(Product::getPrice)
                .filter(Objects::nonNull)
                .toList();

        // Защита от неконсистентных данных: если аналогов нашли, но цен нет — не валимся с NPE.
        if (prices.isEmpty()) {
            return ValuationResultDto.builder()
                    .estimatedPrice(null)
                    .matchedCount((long) matches.size())
                    .condition(condition.name())
                    .yearAdjusted(false)
                    .requestYear(year)
                    .averageYear(null)
                    .yearAdjustmentPercent(null)
                    .note("Аналоги найдены, но у них нет цен для расчёта. Попробуйте другие параметры или дождитесь проверки менеджером.")
                    .build();
        }

        // Цены уже отсортированы по price ASC из Pageable (Sort.by("price").ascending()).
        // Важно: `stream().toList()` возвращает неизменяемый список, поэтому сортировку делать нельзя.
        // Для медианы нам достаточно текущего порядка.
        BigDecimal median = median(prices);

        boolean yearAdjusted = false;
        Integer avgYear = null;
        BigDecimal yearAdjustmentPercent = null;
        BigDecimal estimated = median;

        if (request.hasYear()) {
            List<Integer> years = matches.stream()
                    .map(Product::getYear)
                    .filter(Objects::nonNull)
                    .toList();

            if (!years.isEmpty()) {
                double avg = years.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                avgYear = (int) Math.round(avg);

                int deltaYears = avgYear - year;
                if (deltaYears != 0) {
                    // Упрощённая корректировка:
                    // - если запрос старее средних аналогов: -3% за каждый год (макс -30%)
                    // - если запрос моложе средних аналогов: +1% за каждый год (макс +20%)
                    double percent;
                    if (deltaYears > 0) {
                        percent = Math.min(30.0, deltaYears * 3.0);
                        estimated = estimated.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)));
                        yearAdjustmentPercent = BigDecimal.valueOf(-percent);
                    } else {
                        percent = Math.min(20.0, (-deltaYears) * 1.0);
                        estimated = estimated.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)));
                        yearAdjustmentPercent = BigDecimal.valueOf(percent);
                    }
                    yearAdjusted = true;
                }
            }
        }

        estimated = estimated.setScale(2, RoundingMode.HALF_UP);

        return ValuationResultDto.builder()
                .estimatedPrice(estimated)
                .matchedCount((long) matches.size())
                .condition(condition.name())
                .yearAdjusted(yearAdjusted)
                .requestYear(year)
                .averageYear(avgYear)
                .yearAdjustmentPercent(yearAdjustmentPercent)
                .note("Оценка рассчитана по вашим опубликованным аналогам (PUBLISHED) в тестовом режиме.")
                .build();
    }

    private BigDecimal median(List<BigDecimal> sortedPricesAsc) {
        if (sortedPricesAsc == null || sortedPricesAsc.isEmpty()) return null;
        int n = sortedPricesAsc.size();
        if (n % 2 == 1) {
            return sortedPricesAsc.get(n / 2);
        }
        BigDecimal a = sortedPricesAsc.get(n / 2 - 1);
        BigDecimal b = sortedPricesAsc.get(n / 2);
        return a.add(b).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }
}

