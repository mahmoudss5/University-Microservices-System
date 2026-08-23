package com.unisystem.api_gateway.client.FallBack;

import com.unisystem.api_gateway.client.AcademicCoreServiceClient;
import com.unisystem.api_gateway.dto.DashboardDtos;

import java.util.List;

import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class AcademicCoreServiceClientFallBack implements AcademicCoreServiceClient {
    @Override
    public List<DashboardDtos.EnrolledCourseSummaryDto> getStudentCourses(Long studentId, String authorization, String userId, String roles) {
        return List.of(new DashboardDtos.EnrolledCourseSummaryDto(
                -1L,
                studentId,
                "N/A",
                -1L,
                "ERR",
                "Academic Service Unavailable",
                "N/A",
                0,
                "N/A",
                "N/A",
                "N/A"
        ));
    }

    @Override
    public List<DashboardDtos.CourseDto> getTeacherCourses(Long teacherId, String authorization, String userId, String roles) {
        return List.of(new DashboardDtos.CourseDto(
                -1L,
                "Academic Service Unavailable",
                "ERR",
                "The academic service is currently down.",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now(),
                0,
                0,
                0,
                -1L,
                teacherId
        ));
    }
}
