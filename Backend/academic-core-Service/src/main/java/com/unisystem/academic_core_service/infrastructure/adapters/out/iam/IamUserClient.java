package com.unisystem.academic_core_service.infrastructure.adapters.out.iam;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IamUserClient {

    private static final Logger logger = LoggerFactory.getLogger(IamUserClient.class);
    private static final String UNKNOWN = "Unknown";

    private final IamClient iamClient;

    @CircuitBreaker(name = "iam-service", fallbackMethod = "getTeacherBasicFallback")
    @Retry(name = "iam-service")
    @Bulkhead(name = "iam-service")
    public TeacherBasic getTeacherBasic(Long teacherId, String authHeader) {
        if (teacherId == null) {
            return null;
        }
        IamClient.TeacherBasicResponse response = iamClient.getTeacherBasic(teacherId, authHeader);
        if (response == null) {
            return new TeacherBasic(teacherId, UNKNOWN);
        }
        return new TeacherBasic(response.getId(), resolveName(response.getTeacherName()));
    }

    public TeacherBasic getTeacherBasicFallback(Long teacherId, String authHeader, Throwable t) {
        logger.warn("Fallback: getTeacherBasic teacherId={} cause={}", teacherId, t.getMessage());
        if (teacherId == null) {
            return null;
        }
        return new TeacherBasic(teacherId, UNKNOWN);
    }

    public String getTeacherName(Long teacherId, String authHeader) {
        TeacherBasic teacher = getTeacherBasic(teacherId, authHeader);
        return teacher == null ? UNKNOWN : teacher.name();
    }

    @CircuitBreaker(name = "iam-service", fallbackMethod = "getStudentNameFallback")
    @Retry(name = "iam-service")
    @Bulkhead(name = "iam-service")
    public String getStudentName(Long studentId, String authHeader) {
        if (studentId == null) {
            return UNKNOWN;
        }
        IamClient.StudentBasicResponse response = iamClient.getStudentBasic(studentId, authHeader);
        return response == null ? UNKNOWN : response.resolveUsername();
    }

    public String getStudentNameFallback(Long studentId, String authHeader, Throwable t) {
        logger.warn("Fallback: getStudentName studentId={} cause={}", studentId, t.getMessage());
        return UNKNOWN;
    }

    private String resolveName(String name) {
        return name == null || name.isBlank() ? UNKNOWN : name;
    }

    public record TeacherBasic(Long id, String name) {
    }
}
