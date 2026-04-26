package com.rfq.system.service;

import com.rfq.system.dto.request.CreateUserRequest;
import com.rfq.system.dto.response.UserResponse;
import com.rfq.system.entity.User;
import com.rfq.system.exception.ResourceNotFoundException;
import com.rfq.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(CreateUserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .companyName(request.getCompanyName())
                .build();
        return toResponse(userRepository.save(user));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserResponse getUserById(Long id) {
        return toResponse(getById(id));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .companyName(user.getCompanyName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
