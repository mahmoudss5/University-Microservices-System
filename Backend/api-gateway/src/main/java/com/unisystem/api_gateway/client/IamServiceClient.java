package com.unisystem.api_gateway.client;

import com.unisystem.api_gateway.dto.DashboardDtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "iam-service",fallback = com.unisystem.api_gateway.client.FallBack.IamServiceFallBack.class)
public interface IamServiceClient {

    @GetMapping("/api/students/details/{id}")
    DashboardDtos.StudentProfileDto getStudentDetails(
            @PathVariable("id") Long studentId,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Roles") String roles);

    @GetMapping("/api/teachers/details/{id}")
    DashboardDtos.TeacherProfileDto getTeacherDetails(
            @PathVariable("id") Long teacherId,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Roles") String roles);

    @GetMapping("/api/users/me")
    DashboardDtos.UserDto getCurrentUser(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Roles") String roles);
}
