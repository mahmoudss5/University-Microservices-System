package com.unisystem.academic_core_service.infrastructure.adapters.in.http.services;

import com.unisystem.academic_core_service.domain.application.port.in.CreateAnnouncementUseCase;
import com.unisystem.academic_core_service.domain.application.port.in.GetAnnouncementsQuery;
import com.unisystem.academic_core_service.domain.application.port.in.GetAnnouncementsQuery.AnnouncementDTO;
import com.unisystem.academic_core_service.domain.model.Announcement;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Request.CreateAnnouncementRequest;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.AnnouncementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementHttpService {

    private final CreateAnnouncementUseCase createAnnouncementUseCase;
    private final GetAnnouncementsQuery getAnnouncementsQuery;

    public AnnouncementResponse createAnnouncement(CreateAnnouncementRequest request) {
        Announcement savedAnnouncement = createAnnouncementUseCase.create(
                new CreateAnnouncementUseCase.CreateAnnouncementCommand(
                        request.title(),
                        request.content(),
                        request.courseId(),
                        request.createdAt()));

        return toResponse(savedAnnouncement);
    }

    public List<AnnouncementResponse> getAnnouncementsByCourseId(Long courseId) {
        return getAnnouncementsQuery.getAnnouncementsByCourseId(courseId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AnnouncementResponse> getAnnouncementsByStudentId(Long studentId) {
        return getAnnouncementsQuery.getAnnouncementsByStudentId(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AnnouncementResponse> getAnnouncementsByTeacherId(Long teacherId) {
        return getAnnouncementsQuery.getAnnouncementsByTeacherId(teacherId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AnnouncementResponse toResponse(AnnouncementDTO announcement) {
        return new AnnouncementResponse(
                announcement.id(),
                announcement.title(),
                announcement.content(),
                announcement.courseId(),
                announcement.createdAt());
    }

    private AnnouncementResponse toResponse(Announcement announcement) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getCourseId(),
                announcement.getCreatedAt());
    }
}
