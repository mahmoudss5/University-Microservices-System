package com.unisystem.academic_core_service.infrastructure.adapters.in.http;

import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Request.CreateAnnouncementRequest;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.AnnouncementResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.services.AnnouncementHttpService;
import com.unisystem.academic_core_service.infrastructure.aop.annotations.AuditLog;
import com.unisystem.academic_core_service.infrastructure.aop.annotations.CourseTeacherOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementHttpService announcementHttpService;

    @CourseTeacherOnly(bodyParam = "request")
    @AuditLog(action = "CREATE_ANNOUNCEMENT")
    @PostMapping("/create")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(@RequestBody CreateAnnouncementRequest request) {
        return ResponseEntity.ok(announcementHttpService.createAnnouncement(request));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(announcementHttpService.getAnnouncementsByCourseId(courseId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(announcementHttpService.getAnnouncementsByStudentId(studentId));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsByTeacherId(@PathVariable Long teacherId) {
        return ResponseEntity.ok(announcementHttpService.getAnnouncementsByTeacherId(teacherId));
    }
}
