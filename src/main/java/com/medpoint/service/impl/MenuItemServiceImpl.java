package com.medpoint.service.impl;
import com.medpoint.dto.request.MenuItemRequest;
import com.medpoint.dto.response.MenuItemResponse;
import com.medpoint.entity.MenuItem;
import com.medpoint.repository.MenuItemRepository;
import com.medpoint.service.MenuItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;

    @Override
    public List<MenuItemResponse> getAllActive() {
        return menuItemRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MenuItemResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public MenuItemResponse create(MenuItemRequest request) {
        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .category(request.getCategory())
                .type(request.getType())
                .price(request.getPrice())
                .active(true)
                .build();
        return toResponse(menuItemRepository.save(item));
    }

    @Override
    @Transactional
    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem item = findOrThrow(id);
        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setType(request.getType());
        item.setPrice(request.getPrice());
        return toResponse(menuItemRepository.save(item));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MenuItem item = findOrThrow(id);
        item.setActive(false);
        menuItemRepository.save(item);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private MenuItem findOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "MenuItem not found with id: " + id));
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .type(item.getType())
                .price(item.getPrice())
                .active(item.isActive())
                .build();
    }
}