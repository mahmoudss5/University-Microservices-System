package com.unisystem.academic_core_service.infrastructure.adapters.in.kafka.dto;

import java.util.Map;

public record UserSnapshotEventDto(Long userId, String username, String role, Boolean active) {
    public static UserSnapshotEventDto from(Map<String, Object> payload) {
        Object id = payload.get("userId");
        if (id == null) throw new IllegalArgumentException("userId is required");
        return new UserSnapshotEventDto(
                Long.valueOf(id.toString()),
                value(payload, "username"),
                value(payload, "role"),
                payload.get("active") == null ? Boolean.TRUE : Boolean.valueOf(payload.get("active").toString()));
    }
    private static String value(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null || value.toString().isBlank()) throw new IllegalArgumentException(key + " is required");
        return value.toString();
    }
}
