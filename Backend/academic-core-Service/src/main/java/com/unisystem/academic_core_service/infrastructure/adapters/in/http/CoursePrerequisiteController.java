package com.unisystem.academic_core_service.infrastructure.adapters.in.http;

import com.unisystem.academic_core_service.application.port.in.GetCoursePrerequisitesQuery;
import com.unisystem.academic_core_service.application.port.in.ManageCoursePrerequisitesUseCase;
import com.unisystem.academic_core_service.domain.model.CoursePrerequisite;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/prerequisites")
@RequiredArgsConstructor
public class CoursePrerequisiteController {
    private final ManageCoursePrerequisitesUseCase management;
    private final GetCoursePrerequisitesQuery query;

    @PostMapping("/{prerequisiteCourseId}")
    public ResponseEntity<CoursePrerequisite> add(@PathVariable Long courseId, @PathVariable Long prerequisiteCourseId) {
        return ResponseEntity.ok(management.add(courseId, prerequisiteCourseId));
    }

    @DeleteMapping("/{prerequisiteCourseId}")
    public ResponseEntity<Void> remove(@PathVariable Long courseId, @PathVariable Long prerequisiteCourseId) {
        management.remove(courseId, prerequisiteCourseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CoursePrerequisite>> list(@PathVariable Long courseId) {
        return ResponseEntity.ok(query.findByCourseId(courseId));
    }
}
