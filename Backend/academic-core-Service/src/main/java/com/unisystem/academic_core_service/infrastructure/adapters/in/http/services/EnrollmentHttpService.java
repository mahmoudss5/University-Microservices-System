package com.unisystem.academic_core_service.infrastructure.adapters.in.http.services;

import com.unisystem.academic_core_service.domain.application.port.in.EnrollStudentUseCase;
import com.unisystem.academic_core_service.domain.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.domain.application.port.out.EnrollmentRepositoryPort;
import com.unisystem.academic_core_service.domain.exceptions.InvalidEnrollmentException;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.domain.model.Enrollment;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.EnrolledCourseResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.out.iam.IamUserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentHttpService {

    private final EnrollStudentUseCase enrollStudentUseCase;
    private final EnrollmentRepositoryPort enrollmentRepositoryPort;
    private final CourseRepositoryPort courseRepositoryPort;
    private final IamUserClient iamUserClient;

    public Enrollment enroll(Long studentId, Long courseId) {
        return enrollStudentUseCase.enroll(new EnrollStudentUseCase.EnrollCommand(studentId, courseId));
    }

    public void delete(Long id) {
        enrollmentRepositoryPort.deleteById(id);
    }

    public void drop(Long studentId, Long courseId) {
        enrollStudentUseCase.drop(studentId, courseId);
    }

    public List<EnrolledCourseResponse> getByStudentId(Long studentId, String authorization) {
        List<Enrollment> enrollments = enrollmentRepositoryPort.findByStudentId(studentId);
        String studentName = iamUserClient.getStudentName(studentId, authorization);

        List<Long> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .distinct()
                .toList();

        Map<Long, Course> courseById = courseRepositoryPort.findByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, String> teacherNameById = courseById.values().stream()
                .map(Course::getTeacherId)
                .filter(teacherId -> teacherId != null)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        teacherId -> iamUserClient.getTeacherName(teacherId, authorization)));

        return enrollments.stream()
                .map(enrollment -> {
                    Course course = courseById.get(enrollment.getCourseId());
                    if (course == null) {
                        return null;
                    }
                    return toResponse(
                            enrollment,
                            course,
                            studentName,
                            teacherNameById.getOrDefault(course.getTeacherId(), "Unknown"));
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public List<EnrolledCourseResponse> getByCourseId(Long courseId, String authorization) {
        List<Enrollment> enrollments = enrollmentRepositoryPort.findByCourseId(courseId);
        Course course = courseRepositoryPort.findByIds(List.of(courseId)).stream()
                .findFirst()
                .orElse(null);

        if (course == null) {
            return List.of();
        }

        String teacherName = course.getTeacherId() == null
                ? "Unknown"
                : iamUserClient.getTeacherName(course.getTeacherId(), authorization);

        Map<Long, String> studentNameById = enrollments.stream()
                .map(Enrollment::getStudentId)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        studentId -> iamUserClient.getStudentName(studentId, authorization)));

        return enrollments.stream()
                .map(enrollment -> toResponse(
                        enrollment,
                        course,
                        studentNameById.getOrDefault(enrollment.getStudentId(), "Unknown"),
                        teacherName))
                .toList();
    }

    public Enrollment getByStudentAndCourse(Long studentId, Long courseId) {
        return enrollmentRepositoryPort.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new InvalidEnrollmentException(
                        "Enrollment not found for student " + studentId + " in course " + courseId));
    }

    private EnrolledCourseResponse toResponse(
            Enrollment enrollment,
            Course course,
            String studentName,
            String teacherName) {
        return EnrolledCourseResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudentId())
                .studentName(studentName)
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getName())
                .teacherName(teacherName)
                .credits(course.getCredits())
                .startDate(course.getStartDate() != null ? course.getStartDate().toString() : null)
                .endDate(course.getEndDate() != null ? course.getEndDate().toString() : null)
                .enrollmentDate(enrollment.getEnrolledAt() != null ? enrollment.getEnrolledAt().toString() : null)
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}
