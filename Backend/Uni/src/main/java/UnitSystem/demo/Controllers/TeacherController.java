package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.TeacherService;
import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.TeacherDetailsResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.UserDetailsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@Tag(name = "Teacher", description = "Endpoints for teacher management")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(summary = "Get all teachers")
    @GetMapping
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        List<TeacherResponse> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    @Operation(summary = "Get teacher by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable Long id) {
        TeacherResponse teacher = teacherService.getTeacherById(id);
        if (teacher != null) {
            return ResponseEntity.ok(teacher);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get teacher by username")
    @GetMapping("/username/{userName}")
    public ResponseEntity<TeacherResponse> getTeacherByUserName(@PathVariable String userName) {
        TeacherResponse teacher = teacherService.getTeacherByUserName(userName);
        if (teacher != null) {
            return ResponseEntity.ok(teacher);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a new teacher")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody TeacherRequest teacherRequest) {
        TeacherResponse teacher = teacherService.createTeacher(teacherRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(teacher);
    }

    @Operation(summary = "Update an existing teacher")
    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long id,
            @Valid @RequestBody TeacherRequest teacherRequest) {
        TeacherResponse teacher = teacherService.updateTeacher(id, teacherRequest);
        if (teacher != null) {
            return ResponseEntity.ok(teacher);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete a teacher")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get teacher details with courses")
    @GetMapping("/details/{id}")
    public ResponseEntity<TeacherDetailsResponse> getTeacherDetails(@PathVariable Long id) {
        UserDetailsRequest request = UserDetailsRequest.builder().userId(id).build();
        TeacherDetailsResponse details = teacherService.getTeacherDetails(request);
        return ResponseEntity.ok(details);
    }
}
