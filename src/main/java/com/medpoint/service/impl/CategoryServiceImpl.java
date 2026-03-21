package com.medpoint.service.impl;

import com.medpoint.dto.request.CategoryRequest;
import com.medpoint.dto.response.CategoryResponse;
import com.medpoint.entity.Category;
import com.medpoint.exception.BusinessException;
import com.medpoint.exception.ResourceNotFoundException;
import com.medpoint.repository.CategoryRepository;
import com.medpoint.repository.ProductRepository;
import com.medpoint.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;
    private final ProductRepository  productRepo;

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        if (categoryRepo.existsByNameIgnoreCase(req.getName())) {
            throw new BusinessException("Category '" + req.getName() + "' already exists.");
        }
        Category saved = categoryRepo.save(Category.builder().name(req.getName()).build());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse rename(Long id, CategoryRequest req) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (categoryRepo.existsByNameIgnoreCase(req.getName())) {
            throw new BusinessException("Category '" + req.getName() + "' already exists.");
        }

        String oldName = category.getName();
        category.setName(req.getName());
        categoryRepo.save(category);

        // Keep all products' category string in sync
        productRepo.findByCategory(oldName)
                .forEach(p -> {
                    p.setCategory(req.getName());
                    productRepo.save(p);
                });

        return toResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        boolean inUse = productRepo.existsByCategory(category.getName());
        if (inUse) {
            throw new BusinessException(
                    "Cannot delete '" + category.getName() + "' — it is assigned to one or more products."
            );
        }
        categoryRepo.delete(category);
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder().id(c.getId()).name(c.getName()).build();
    }
}