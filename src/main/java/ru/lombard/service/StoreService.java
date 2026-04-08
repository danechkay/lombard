package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lombard.dto.StoreDto;
import ru.lombard.repository.StoreRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public List<StoreDto> listActive() {
        return storeRepository.findByActiveTrueOrderBySortOrderAscIdAsc().stream()
                .map(s -> StoreDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .slug(s.getSlug())
                        .address(s.getAddress())
                        .build())
                .collect(Collectors.toList());
    }
}
