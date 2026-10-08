package com.taskmanager.service;

import com.taskmanager.dto.AuthResponse;
import com.taskmanager.dto.LoginRequest;
import com.taskmanager.dto.RegisterRequest;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        // SECURITY ENFORCEMENT:
        // All new user registrations are ALWAYS assigned INDIVIDUAL_USER role.
        // Role cannot be chosen or passed by client.
        User.Role role = User.Role.INDIVIDUAL_USER;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .phone(request.getPhone())
                .department(request.getDepartment())
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .user(toUserDto(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .user(toUserDto(user))
                .build();
    }

    public List<AuthResponse.UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    public List<AuthResponse.UserDto> getUsersByRole(String role) {
        User.Role userRole = User.Role.fromString(role);
        return userRepository.findByRole(userRole).stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    public AuthResponse.UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toUserDto(user);
    }

    public AuthResponse.UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toUserDto(user);
    }

    public AuthResponse.UserDto updateUserRole(Long id, String roleStr) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (roleStr == null || roleStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }

        String norm = roleStr.trim().toUpperCase();
        User.Role targetRole;
        switch (norm) {
            case "ADMIN":
                targetRole = User.Role.ADMIN;
                break;
            case "MANAGER":
                targetRole = User.Role.MANAGER;
                break;
            case "TEAM_MEMBER":
            case "TEAM":
                targetRole = User.Role.TEAM_MEMBER;
                break;
            case "INDIVIDUAL_USER":
            case "USER":
                targetRole = User.Role.INDIVIDUAL_USER;
                break;
            default:
                throw new IllegalArgumentException("Invalid role: " + roleStr + ". Valid roles are: ADMIN, MANAGER, TEAM_MEMBER, INDIVIDUAL_USER");
        }

        user.setRole(targetRole);
        user = userRepository.save(user);
        return toUserDto(user);
    }

    public AuthResponse.UserDto updateUser(Long id, RegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getRole() != null) {
            try {
                user.setRole(User.Role.fromString(request.getRole()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return toUserDto(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<AuthResponse.UserDto> searchUsers(String query) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
                .stream().map(this::toUserDto).collect(Collectors.toList());
    }

    private AuthResponse.UserDto toUserDto(User user) {
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .build();
    }
}
