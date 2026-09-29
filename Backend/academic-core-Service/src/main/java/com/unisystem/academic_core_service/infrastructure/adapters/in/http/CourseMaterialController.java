package com.unisystem.academic_core_service.infrastructure.adapters.in.http;

import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Request.CreateMaterialUploadRequest;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.CourseMaterialResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.UploadUrlResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.services.CourseMaterialHttpService;
import com.unisystem.academic_core_service.infrastructure.aop.annotations.CourseTeacherOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses/{courseId}/materials")
@RequiredArgsConstructor
public class CourseMaterialController {

    private final CourseMaterialHttpService materialHttpService;

    @PostMapping("/upload-url")
    @CourseTeacherOnly(param = "courseId")
    public ResponseEntity<UploadUrlResponse> initiateUpload(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateMaterialUploadRequest request
    ) {
        return ResponseEntity.ok(
                materialHttpService.initiateUpload(courseId, request)
        );
    }

    @PostMapping("/{materialId}/complete")
    @CourseTeacherOnly(param = "courseId")
    public ResponseEntity<CourseMaterialResponse> completeUpload(
            @PathVariable Long courseId,
            @PathVariable Long materialId
    ) {
        return ResponseEntity.ok(
                materialHttpService.completeUpload(courseId, materialId)
        );
    }

    @GetMapping
    public ResponseEntity<List<CourseMaterialResponse>> findByCourseId(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(materialHttpService.findByCourseId(courseId));
    }

    @GetMapping("/{materialId}/download-url")
    public ResponseEntity<Map<String, URI>> createDownloadUrl(
            @PathVariable Long courseId,
            @PathVariable Long materialId
    ) {
        return ResponseEntity.ok(Map.of(
                "downloadUrl",
                materialHttpService.createDownloadUrl(courseId, materialId)
        ));
    }

    @DeleteMapping("/{materialId}")
    @CourseTeacherOnly(param = "courseId")
    public ResponseEntity<Void> delete(
            @PathVariable Long courseId,
            @PathVariable Long materialId
    ) {
        materialHttpService.delete(courseId, materialId);
        return ResponseEntity.noContent().build();
    }
}
