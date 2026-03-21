package com.medpoint.service;
import com.medpoint.dto.request.RoomRequest;
import com.medpoint.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {
    List<RoomResponse> getAll();
    RoomResponse getById(Long id);
    RoomResponse create(RoomRequest request);
    RoomResponse update(Long id, RoomRequest request);
    void delete(Long id);
}