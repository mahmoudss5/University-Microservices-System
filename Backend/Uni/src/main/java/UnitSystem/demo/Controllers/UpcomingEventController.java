package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UpcomingEventService;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventRequest;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Upcoming Events", description = "Endpoints for managing upcoming events and deadlines")
@RequiredArgsConstructor
public class UpcomingEventController {

    private final UpcomingEventService upcomingEventService;

    @Operation(summary = "Get all events")
    @GetMapping
    public ResponseEntity<List<UpcomingEventResponse>> getAllEvents() {
        return ResponseEntity.ok(upcomingEventService.getAllEvents());
    }

    @Operation(summary = "Get upcoming events (from now onwards, ordered by date)")
    @GetMapping("/upcoming")
    public ResponseEntity<List<UpcomingEventResponse>> getUpcomingEvents() {
        return ResponseEntity.ok(upcomingEventService.getUpcomingEvents());
    }

    @Operation(summary = "Get events by type (HIGH_PRIORITY, EXAM, EVENT)")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<UpcomingEventResponse>> getEventsByType(@PathVariable String type) {
        return ResponseEntity.ok(upcomingEventService.getEventsByType(type));
    }

    @Operation(summary = "Get events belonging to a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UpcomingEventResponse>> getEventsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(upcomingEventService.getEventsByUser(userId));
    }

    @Operation(summary = "Get event by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UpcomingEventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(upcomingEventService.getEventById(id));
    }

    @Operation(summary = "Create a new event")
    @PostMapping
    public ResponseEntity<UpcomingEventResponse> createEvent(@RequestBody UpcomingEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(upcomingEventService.createEvent(request));
    }

    @Operation(summary = "Update an existing event")
    @PutMapping("/{id}")
    public ResponseEntity<UpcomingEventResponse> updateEvent(@PathVariable Long id,
            @RequestBody UpcomingEventRequest request) {
        return ResponseEntity.ok(upcomingEventService.updateEvent(id, request));
    }

    @Operation(summary = "Delete an event")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        upcomingEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
