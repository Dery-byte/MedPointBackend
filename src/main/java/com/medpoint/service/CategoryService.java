package com.medpoint.service;
import com.medpoint.dto.request.CategoryRequest;
import com.medpoint.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAll();
    CategoryResponse create(CategoryRequest request);
    CategoryResponse rename(Long id, CategoryRequest request);
    void delete(Long id);
}