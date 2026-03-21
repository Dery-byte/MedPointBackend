package com.medpoint.service.impl;
import com.medpoint.dto.request.CreateUserRequest;
import com.medpoint.dto.request.UpdateUserRequest;
import com.medpoint.dto.response.UserResponse;
import com.medpoint.entity.User;
import com.medpoint.enums.StaffRole;
import com.medpoint.exception.BusinessException;
import com.medpoint.exception.ResourceNotFoundException;
import com.medpoint.repository.UserRepository;
import com.medpoint.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email already in use: " + req.getEmail());
        }
        User user = User.builder()
                .name(req.getName())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .accessModules(new HashSet<>(req.getAccessModules()))
                .manageModules(req.getManageModules() != null ? new HashSet<>(req.getManageModules()) : new HashSet<>())
                .active(true)
                .build();
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = findOrThrow(id);
        if (user.getRole() == StaffRole.SUPERADMIN) {
            throw new BusinessException("Cannot edit a Super Admin account.");
        }
        user.setName(req.getName());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRole(req.getRole());
        user.setAccessModules(new HashSet<>(req.getAccessModules()));
        user.setManageModules(req.getManageModules() != null ? new HashSet<>(req.getManageModules()) : new HashSet<>());
        if (req.getPassword() != null && req.getPassword().length() >= 6) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse toggleActive(Long id) {
        User user = findOrThrow(id);
        if (user.getRole() == StaffRole.SUPERADMIN) {
            throw new BusinessException("Cannot deactivate a Super Admin account.");
        }
        user.setActive(!user.isActive());
        return toResponse(userRepository.save(user));
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId()).name(u.getName()).email(u.getEmail()).firstName(u.getFirstName()).lastName(u.getLastName())
                .role(u.getRole()).accessModules(u.getAccessModules())
                .manageModules(u.getManageModules()).active(u.isActive())
                .createdAt(u.getCreatedAt()).build();
    }
}
