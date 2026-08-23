package com.unisystem.api_gateway.client.FallBack;

import com.unisystem.api_gateway.client.IamServiceClient;
import com.unisystem.api_gateway.dto.DashboardDtos;
import org.springframework.stereotype.Component;

@Component

public class IamServiceFallBack implements IamServiceClient {

    @Override
    public DashboardDtos.StudentProfileDto getStudentDetails(Long studentId, String authorization, String userId, String roles) {
        return new DashboardDtos.StudentProfileDto(
                studentId,
                roles != null ? roles : "STUDENT",
                "Service Unavailable",
                "N/A",
                java.math.BigDecimal.ZERO,
                0,
                java.util.Collections.emptyList(),
                0,
                0,
                "Unknown",
                java.util.Collections.emptyList(),
                java.util.Collections.emptyList()
        );
    }

    @Override
    public DashboardDtos.TeacherProfileDto getTeacherDetails(Long teacherId, String authorization, String userId, String roles) {
        return new DashboardDtos.TeacherProfileDto(
                teacherId,
                roles != null ? roles : "TEACHER",
                "Service Unavailable",
                "N/A",
                java.math.BigDecimal.ZERO,
                "Unknown",
                java.util.Collections.emptyList(),
                java.util.Collections.emptyList(),
                java.util.Collections.emptyList(),
                0,
                0
        );
    }

    @Override
    public DashboardDtos.UserDto getCurrentUser(String authorization, String userId, String roles) {
        Long parsedUserId = null;
        if (userId != null && !userId.isEmpty()) {
            try {
                parsedUserId = Long.parseLong(userId);
            } catch (NumberFormatException ignored) {}
        }
        
        return new DashboardDtos.UserDto(
                parsedUserId,
                "Service Unavailable",
                "N/A",
                roles != null ? roles : "USER"
        );
    }
}
