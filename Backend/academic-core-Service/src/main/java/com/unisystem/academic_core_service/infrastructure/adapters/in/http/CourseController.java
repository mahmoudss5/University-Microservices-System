package com.unisystem.academic_core_service.infrastructure.adapters.in.http;

import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Request.CreateCourseRequest;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Request.UpdateCourseRequest;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.CourseCardResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.CoureseDetailsResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.services.CourseHttpService;
import com.unisystem.academic_core_service.infrastructure.aop.annotations.AuditLog;
import com.unisystem.academic_core_service.infrastructure.aop.annotations.CourseTeacherOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseHttpService courseHttpService;

    @AuditLog(action = "CREATE_COURSE")
    @CourseTeacherOnly(requireCourseOwnership = false)
    @PostMapping
    public ResponseEntity<Course> createCourse(
            @RequestBody CreateCourseRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return ResponseEntity.ok(courseHttpService.createCourse(request, userIdHeader));
    }

    @CourseTeacherOnly(param = "id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseHttpService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @AuditLog(action = "UPDATE_COURSE")
    @CourseTeacherOnly(param = "id")
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody UpdateCourseRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return ResponseEntity.ok(courseHttpService.updateCourse(id, request, userIdHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoureseDetailsResponse> getCourseById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(courseHttpService.getCourseById(id, authHeader));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CourseCardResponse>> getAllCourses(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(courseHttpService.getAllCourses(authHeader));
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<Course>> getCoursesByIds(@RequestBody CourseIdsRequest request) {
        return ResponseEntity.ok(courseHttpService.getCoursesByIds(request == null ? null : request.ids()));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Course>> getPopularCourses(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(courseHttpService.getPopularCourses(limit));
    }

    @GetMapping("/teacher/name/{teacherName}")
    public ResponseEntity<List<Course>> getCoursesByTeacherName(@PathVariable String teacherName) {
        return ResponseEntity.ok(courseHttpService.getCoursesByTeacherName(teacherName));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Course>> getCoursesByTeacherId(@PathVariable Long teacherId) {
        return ResponseEntity.ok(courseHttpService.getCoursesByTeacherId(teacherId));
    }

    @GetMapping("/Department/{departmentName}")
    public ResponseEntity<List<Course>> getCoursesByDepartmentName(@PathVariable String departmentName) {
        return ResponseEntity.ok(courseHttpService.getCoursesByDepartmentName(departmentName));
    }

    public record CourseIdsRequest(List<Long> ids) {
    }
}
