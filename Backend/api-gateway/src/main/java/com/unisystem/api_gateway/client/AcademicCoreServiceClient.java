package com.unisystem.api_gateway.client;
import com.unisystem.api_gateway.dto.DashboardDtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "academic-core" ,fallback = com.unisystem.api_gateway.client.FallBack.AcademicCoreServiceClientFallBack.class)
public interface AcademicCoreServiceClient {

    @GetMapping("/api/enrolled-courses/student/{id}")
    List<DashboardDtos.EnrolledCourseSummaryDto> getStudentCourses(
            @PathVariable("id") Long studentId,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Roles") String roles);

    @GetMapping("/api/courses/teacher/{id}")
    List<DashboardDtos.CourseDto> getTeacherCourses(
            @PathVariable("id") Long teacherId,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Roles") String roles);
}
