package com.medpoint.service;
import com.medpoint.dto.request.RoomCategoryRequest;
import com.medpoint.dto.response.RoomCategoryResponse;
import com.medpoint.entity.RoomCategory;

import java.util.List;

public interface RoomCategoryService {
    List<RoomCategoryResponse> getAll();
    RoomCategoryResponse getById(Long id);
    RoomCategoryResponse create(RoomCategoryRequest request);
    RoomCategoryResponse update(Long id, RoomCategoryRequest request);
    void delete(Long id);

    /** Used internally by RoomServiceImpl to resolve categoryId → entity */
    RoomCategory findEntityOrThrow(Long id);

    /** Used internally by RoomServiceImpl to map entity → response */
    RoomCategoryResponse toResponse(RoomCategory category);
}