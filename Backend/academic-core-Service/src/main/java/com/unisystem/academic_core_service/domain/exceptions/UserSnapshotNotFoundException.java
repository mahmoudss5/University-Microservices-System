package com.unisystem.academic_core_service.domain.exceptions;

public class UserSnapshotNotFoundException extends RuntimeException {
    public UserSnapshotNotFoundException(Long userId) { super("User " + userId + " was not found in the academic user snapshot"); }
}
