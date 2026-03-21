package com.medpoint.service.impl;
import com.medpoint.dto.request.RoomCategoryRequest;
import com.medpoint.dto.response.RoomCategoryResponse;
import com.medpoint.entity.RoomCategory;
import com.medpoint.repository.RoomCategoryRepository;
import com.medpoint.service.RoomCategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomCategoryServiceImpl implements RoomCategoryService {

    private final RoomCategoryRepository roomCategoryRepository;

    @Override
    public List<RoomCategoryResponse> getAll() {
        return roomCategoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomCategoryResponse getById(Long id) {
        return toResponse(findEntityOrThrow(id));
    }

    @Override
    @Transactional
    public RoomCategoryResponse create(RoomCategoryRequest request) {
        if (roomCategoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Room category already exists: " + request.getName());
        }
        RoomCategory category = RoomCategory.builder()
                .name(request.getName())
                .pricePerNight(request.getPricePerNight())
                .build();
        return toResponse(roomCategoryRepository.save(category));
    }

    @Override
    @Transactional
    public RoomCategoryResponse update(Long id, RoomCategoryRequest request) {
        RoomCategory category = findEntityOrThrow(id);
        if (!category.getName().equals(request.getName())
                && roomCategoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Room category name already in use: " + request.getName());
        }
        category.setName(request.getName());
        category.setPricePerNight(request.getPricePerNight());
        return toResponse(roomCategoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        roomCategoryRepository.delete(findEntityOrThrow(id));
    }

    @Override
    public RoomCategory findEntityOrThrow(Long id) {
        return roomCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "RoomCategory not found with id: " + id));
    }

    @Override
    public RoomCategoryResponse toResponse(RoomCategory category) {
        return RoomCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .pricePerNight(category.getPricePerNight())
                .build();
    }
}