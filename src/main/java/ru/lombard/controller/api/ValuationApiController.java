package ru.lombard.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.lombard.dto.ValuationRequestCreateResponseDto;
import ru.lombard.dto.ValuationRequestDto;
import ru.lombard.dto.ValuationResultDto;
import ru.lombard.service.ValuationService;
import ru.lombard.service.ValuationRequestService;
import ru.lombard.service.CurrentUserService;
import ru.lombard.entity.User;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/valuation")
@RequiredArgsConstructor
public class ValuationApiController {

    private final ValuationService valuationService;
    private final ValuationRequestService valuationRequestService;
    private final CurrentUserService currentUserService;

    @PostMapping("/calculate")
    public ValuationResultDto calculate(@RequestBody ValuationRequestDto request) {
        return valuationService.calculate(request);
    }

    /**
     * Создает заявку на оценку для менеджера (даже если аналогов в базе не найдено).
     */
    @PostMapping("/requests")
    public ValuationRequestCreateResponseDto createRequest(@RequestBody ValuationRequestDto request) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Нужна авторизация"));
        return valuationRequestService.createRequest(user, request);
    }
}

