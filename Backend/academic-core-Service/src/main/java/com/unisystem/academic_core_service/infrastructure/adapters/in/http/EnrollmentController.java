package com.unisystem.academic_core_service.infrastructure.adapters.in.http;

import com.unisystem.academic_core_service.domain.model.Enrollment;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.EnrolledCourseResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.services.EnrollmentHttpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/enrolled-courses")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentHttpService enrollmentHttpService;

    @PostMapping
    public ResponseEntity<Enrollment> enroll(@RequestBody EnrollRequest request) {
        return ResponseEntity.ok(enrollmentHttpService.enroll(request.studentId(), request.courseId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enrollmentHttpService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/drop")
    public ResponseEntity<Void> drop(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        enrollmentHttpService.drop(studentId, courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrolledCourseResponse>> getByStudentId(
            @PathVariable Long studentId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(enrollmentHttpService.getByStudentId(studentId, authorization));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrolledCourseResponse>> getByCourseId(
            @PathVariable Long courseId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(enrollmentHttpService.getByCourseId(courseId, authorization));
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<Enrollment> getByStudentAndCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentHttpService.getByStudentAndCourse(studentId, courseId));
    }

    public record EnrollRequest(Long studentId, Long courseId) {
    }

    @PatchMapping("/student/{studentId}/course/{courseId}/result")
    public ResponseEntity<Enrollment> complete(
            @PathVariable Long studentId, @PathVariable Long courseId, @RequestBody CompletionRequest request) {
        return ResponseEntity.ok(enrollmentHttpService.complete(studentId, courseId, request.grade(), request.passed()));
    }

    public record CompletionRequest(BigDecimal grade, boolean passed) {}

}
