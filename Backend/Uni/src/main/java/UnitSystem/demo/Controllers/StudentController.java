package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.StudentService;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.StudentDetailsResponse;
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
@RequestMapping("/api/students")
@Tag(name = "Student", description = "Endpoints for student management")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Get all students")
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        List<StudentResponse> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Get student by ID")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        StudentResponse student = studentService.getStudentById(id);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get student by username")
    @GetMapping("/username/{userName}")
    public ResponseEntity<StudentResponse> getStudentByUserName(@PathVariable String userName) {
        StudentResponse student = studentService.getStudentByUserName(userName);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a new student")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest studentRequest) {
        StudentResponse student = studentService.createStudent(studentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(student);
    }

    @Operation(summary = "Update an existing student")
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id,
            @Valid @RequestBody StudentRequest studentRequest) {
        StudentResponse student = studentService.updateStudent(id, studentRequest);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete a student")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get student details with enrolled courses and academic standing")
    @GetMapping("/details/{id}")
    public ResponseEntity<StudentDetailsResponse> getStudentDetails(@PathVariable Long id) {
        UserDetailsRequest request = UserDetailsRequest.builder().userId(id).build();
        StudentDetailsResponse details = studentService.getStudentDetails(request);
        return ResponseEntity.ok(details);
    }
}
