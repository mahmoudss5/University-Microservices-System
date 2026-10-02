package com.unisystem.academic_core_service.infrastructure.adapters.out.iam;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


@FeignClient(name = "iam-service")
public interface IamClient {

    @GetMapping("/api/teachers/basic/{teacherId}")
    TeacherBasicResponse getTeacherBasic(
            @PathVariable Long teacherId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    );

    @GetMapping("/api/teachers/basic/all")
    List<TeacherBasicResponse> getAllTeacherBasics();

    @GetMapping("/api/students/basic/{id}")
    StudentBasicResponse getStudentBasic(
            @PathVariable("id") Long studentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    );

    @GetMapping("/api/students/basic/all")
    List<StudentBasicResponse> getAllStudentBasics();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class TeacherBasicResponse {
        private Long id;
        @JsonAlias({"name", "fullName", "username", "userName", "teacherUsername", "teacherName"})
        private String teacherName;
        private String officeLocation;
        private String role;
        private boolean active;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class StudentBasicResponse {
        private Long id;
        @JsonAlias({"username", "userName", "name", "fullName", "studentName"})
        private String username;
        private String role;
        private boolean active;

          public String resolveUsername() {
            return (username == null || username.isBlank()) ? "Unknown" : username;
        }
    }
}
