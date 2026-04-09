package ru.lombard.controller.api.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lombard.dto.ValuationRequestRowDto;
import ru.lombard.dto.ValuationSetPriceRequestDto;
import ru.lombard.entity.User;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.ValuationRequestService;

import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/admin/valuations")
@RequiredArgsConstructor
public class AdminValuationApiController {

    private final ValuationRequestService valuationRequestService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public Page<ValuationRequestRowDto> list(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page) {
        return valuationRequestService.findAll(page, 50);
    }

    @PostMapping("/{id}/price")
    public ValuationRequestRowDto setPrice(
            @PathVariable Long id,
            @RequestBody ValuationSetPriceRequestDto request
    ) {
        User manager = currentUserService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Нужна авторизация"));
        return valuationRequestService.setPrice(manager, id, request);
    }
}

