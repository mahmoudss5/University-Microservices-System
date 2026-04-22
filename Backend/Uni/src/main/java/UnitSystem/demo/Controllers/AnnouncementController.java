package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.AnnouncementService;
import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@Tag(name = "Announcement Controller", description = "APIs for managing announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "Create a new announcement")
    @PostMapping("/create")
    public ResponseEntity<Void> create(@RequestBody AnnouncementRequest request) {
        announcementService.createAnnouncement(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Delete an announcement by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get an announcement by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getById(@PathVariable Long id) {
        AnnouncementResponse response = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all announcements for a course")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<AnnouncementResponse>> getByCourseId(@PathVariable Long courseId) {
        List<AnnouncementResponse> responses = announcementService.getAnnouncementsByCourseId(courseId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get all announcements")
    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAll() {
        List<AnnouncementResponse> responses = announcementService.getAllAnnouncements();
        return ResponseEntity.ok(responses);
    }
}
