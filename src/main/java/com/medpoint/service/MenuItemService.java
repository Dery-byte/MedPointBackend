package com.medpoint.service;

import com.medpoint.dto.request.MenuItemRequest;
import com.medpoint.dto.response.MenuItemResponse;

import java.util.List;

public interface MenuItemService {
    List<MenuItemResponse> getAllActive();
    MenuItemResponse getById(Long id);
    MenuItemResponse create(MenuItemRequest request);
    MenuItemResponse update(Long id, MenuItemRequest request);
    void delete(Long id);
}