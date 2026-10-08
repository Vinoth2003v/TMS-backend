package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "phone")
    private String phone;

    @Column(name = "department")
    private String department;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Required by JPA
    public User() {}

    // Builder constructor
    @Builder
    public User(
            Long id,
            String name,
            String email,
            String password,
            Role role,
            String avatarUrl,
            String phone,
            String department,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
        this.department = department;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartment() {
        return department;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDepartment(String dept) {
        this.department = dept;
    }

    public void setCreatedAt(LocalDateTime t) {
        this.createdAt = t;
    }

    public void setUpdatedAt(LocalDateTime t) {
        this.updatedAt = t;
    }

    public enum Role {
        ADMIN,
        MANAGER,
        TEAM_MEMBER,
        INDIVIDUAL_USER;

        public static Role fromString(String val) {
            if (val == null) return INDIVIDUAL_USER;
            String normalized = val.trim().toUpperCase();
            switch (normalized) {
                case "ADMIN":
                    return ADMIN;
                case "MANAGER":
                    return MANAGER;
                case "TEAM_MEMBER":
                case "TEAM":
                    return TEAM_MEMBER;
                case "INDIVIDUAL_USER":
                case "USER":
                    return INDIVIDUAL_USER;
                default:
                    try {
                        return Role.valueOf(normalized);
                    } catch (Exception e) {
                        return INDIVIDUAL_USER;
                    }
            }
        }
    }
}