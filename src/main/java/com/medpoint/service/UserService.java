package com.medpoint.service;
import com.medpoint.dto.request.CreateUserRequest;
import com.medpoint.dto.request.UpdateUserRequest;
import com.medpoint.dto.response.UserResponse;
import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    UserResponse toggleActive(Long id);
}
