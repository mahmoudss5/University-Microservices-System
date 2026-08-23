package com.unisystem.api_gateway.client.caller;

import com.unisystem.api_gateway.client.IamServiceClient;
import com.unisystem.api_gateway.client.FallBack.IamServiceFallBack;
import com.unisystem.api_gateway.dto.DashboardDtos;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IamServiceCaller {

    private final IamServiceClient iamServiceClient;
    private final IamServiceFallBack fallback;

    @CircuitBreaker(name = "iam-service", fallbackMethod = "getStudentDetailsFallback")
    @Retry(name = "iam-service")
    @Bulkhead(name = "iam-service")
    public DashboardDtos.StudentProfileDto getStudentDetails(
            Long studentId, String authorization, String userId, String roles) {
        return iamServiceClient.getStudentDetails(studentId, authorization, userId, roles);
    }

    public DashboardDtos.StudentProfileDto getStudentDetailsFallback(
            Long studentId, String authorization, String userId, String roles, Throwable t) {
        return fallback.getStudentDetails(studentId, authorization, userId, roles);
    }

    @CircuitBreaker(name = "iam-service", fallbackMethod = "getTeacherDetailsFallback")
    @Retry(name = "iam-service")
    @Bulkhead(name = "iam-service")
    public DashboardDtos.TeacherProfileDto getTeacherDetails(
            Long teacherId, String authorization, String userId, String roles) {
        return iamServiceClient.getTeacherDetails(teacherId, authorization, userId, roles);
    }

    public DashboardDtos.TeacherProfileDto getTeacherDetailsFallback(
            Long teacherId, String authorization, String userId, String roles, Throwable t) {
        return fallback.getTeacherDetails(teacherId, authorization, userId, roles);
    }

    @CircuitBreaker(name = "iam-service", fallbackMethod = "getCurrentUserFallback")
    @Retry(name = "iam-service")
    @Bulkhead(name = "iam-service")
    public DashboardDtos.UserDto getCurrentUser(
            String authorization, String userId, String roles) {
        return iamServiceClient.getCurrentUser(authorization, userId, roles);
    }

    public DashboardDtos.UserDto getCurrentUserFallback(
            String authorization, String userId, String roles, Throwable t) {
        return fallback.getCurrentUser(authorization, userId, roles);
    }
}
