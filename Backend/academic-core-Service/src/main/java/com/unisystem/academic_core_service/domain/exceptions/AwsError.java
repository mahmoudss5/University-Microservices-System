package com.unisystem.academic_core_service.domain.exceptions;

public class AwsError extends RuntimeException {
    public AwsError(String message) {
        super(message);
    }

    public AwsError(String message, Throwable cause) {
        super(message, cause);
    }
}
