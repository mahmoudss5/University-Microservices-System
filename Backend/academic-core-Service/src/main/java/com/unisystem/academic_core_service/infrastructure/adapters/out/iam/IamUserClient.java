package com.unisystem.academic_core_service.infrastructure.adapters.out.iam;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IamUserClient {

    private static final String UNKNOWN = "Unknown";

    private final IamClient iamClient;

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

    public String getTeacherName(Long teacherId, String authHeader) {
        TeacherBasic teacher = getTeacherBasic(teacherId, authHeader);
        return teacher == null ? UNKNOWN : teacher.name();
    }

    public String getStudentName(Long studentId, String authHeader) {
        if (studentId == null) {
            return UNKNOWN;
        }
        IamClient.StudentBasicResponse response = iamClient.getStudentBasic(studentId, authHeader);
        return response == null ? UNKNOWN : response.resolveUsername();
    }

    private String resolveName(String name) {
        return name == null || name.isBlank() ? UNKNOWN : name;
    }

    public record TeacherBasic(Long id, String name) {
    }
}
