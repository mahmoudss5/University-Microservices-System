package com.unisystem.academic_core_service.domain.model;

import java.time.LocalDateTime;

public record UserSnapshot(Long userId, String userName, UserRole role, boolean active, LocalDateTime updatedAt) {
    public UserSnapshot {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
        if (userName == null || userName.isBlank()) throw new IllegalArgumentException("User name is required");
        if (role == null) throw new IllegalArgumentException("User role is required");
    }
}
