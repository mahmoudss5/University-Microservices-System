package com.unisystem.academic_core_service.domain.exceptions;

import com.unisystem.academic_core_service.domain.model.UserRole;

public class InvalidUserRoleException extends RuntimeException {
    public InvalidUserRoleException(Long userId, UserRole expected) { super("User " + userId + " must be an active " + expected); }
}
