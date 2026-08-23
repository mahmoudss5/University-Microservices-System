package com.unisystem.api_gateway.client.caller;

import com.unisystem.api_gateway.client.AcademicCoreServiceClient;
import com.unisystem.api_gateway.client.FallBack.AcademicCoreServiceClientFallBack;
import com.unisystem.api_gateway.dto.DashboardDtos;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicServiceCaller {

    private final AcademicCoreServiceClient academicCoreServiceClient;
    private final AcademicCoreServiceClientFallBack fallback;

    @CircuitBreaker(name = "academic-core", fallbackMethod = "getStudentCoursesFallback")
    @Retry(name = "academic-core")
    @Bulkhead(name = "academic-core")
    public List<DashboardDtos.EnrolledCourseSummaryDto> getStudentCourses(
            Long studentId, String authorization, String userId, String roles) {
        return academicCoreServiceClient.getStudentCourses(studentId, authorization, userId, roles);
    }

    public List<DashboardDtos.EnrolledCourseSummaryDto> getStudentCoursesFallback(
            Long studentId, String authorization, String userId, String roles, Throwable t) {
        return fallback.getStudentCourses(studentId, authorization, userId, roles);
    }

    @CircuitBreaker(name = "academic-core", fallbackMethod = "getTeacherCoursesFallback")
    @Retry(name = "academic-core")
    @Bulkhead(name = "academic-core")
    public List<DashboardDtos.CourseDto> getTeacherCourses(
            Long teacherId, String authorization, String userId, String roles) {
        return academicCoreServiceClient.getTeacherCourses(teacherId, authorization, userId, roles);
    }

    public List<DashboardDtos.CourseDto> getTeacherCoursesFallback(
            Long teacherId, String authorization, String userId, String roles, Throwable t) {
        return fallback.getTeacherCourses(teacherId, authorization, userId, roles);
    }
}
