package com.medpoint.service.impl;
import com.medpoint.dto.request.RoomRequest;
import com.medpoint.dto.response.RoomCategoryResponse;
import com.medpoint.dto.response.RoomResponse;
import com.medpoint.entity.Room;
import com.medpoint.entity.RoomCategory;
import com.medpoint.repository.RoomRepository;
import com.medpoint.service.RoomCategoryService;
import com.medpoint.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomCategoryService roomCategoryService;

    @Override
    public List<RoomResponse> getAll() {
        return roomRepository.findAllByOrderByRoomNumberAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public RoomResponse create(RoomRequest request) {
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new IllegalArgumentException(
                    "Room number already exists: " + request.getRoomNumber());
        }
        RoomCategory category = roomCategoryService.findEntityOrThrow(request.getCategoryId());
        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .category(category)
                .status(request.getStatus())
                .build();
        return toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = findOrThrow(id);
        if (!room.getRoomNumber().equals(request.getRoomNumber())
                && roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new IllegalArgumentException(
                    "Room number already in use: " + request.getRoomNumber());
        }
        RoomCategory category = roomCategoryService.findEntityOrThrow(request.getCategoryId());
        room.setRoomNumber(request.getRoomNumber());
        room.setCategory(category);
        room.setStatus(request.getStatus());
        return toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        roomRepository.delete(findOrThrow(id));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Room findOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Room not found with id: " + id));
    }

    private RoomResponse toResponse(Room room) {
        RoomCategoryResponse categoryResponse = roomCategoryService.toResponse(room.getCategory());
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .category(categoryResponse)
                .status(room.getStatus())
                .build();
    }
}