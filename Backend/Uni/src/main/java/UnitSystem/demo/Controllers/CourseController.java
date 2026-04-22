package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.CourseService;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course", description = "Endpoints for course management")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Get all courses")
    @GetMapping("all")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        List<CourseResponse> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Get most popular courses")
    @GetMapping("/popular/{topN}")
    public ResponseEntity<List<CourseResponse>> getMostPopularCourses(@PathVariable int topN) {
        List<CourseResponse> courses = courseService.getMostPopularCourses(topN);
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Get course by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        CourseResponse course = courseService.getCourseById(id);
        if (course != null) {
            return ResponseEntity.ok(course);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a new course")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest courseRequest) {
        CourseResponse course = courseService.createCourse(courseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    @Operation(summary = "Update an existing course")
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id,
                                                       @Valid @RequestBody CourseRequest courseRequest) {
        CourseResponse course = courseService.updateCourse(courseRequest, id);
        if (course != null) {
            return ResponseEntity.ok(course);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete a course")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "get most popular courses")
    @GetMapping("/popular")
    public ResponseEntity<List<CourseResponse>> getMostPopularCourses() {
        List<CourseResponse> courses = courseService.getMostPopularCourses(4);
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Get courses by department name")
    @GetMapping("/department/{departmentName}")
    public ResponseEntity<List<CourseResponse>> getCoursesByDepartment(@PathVariable String departmentName) {
        List<CourseResponse> courses = courseService.getCoursesByDepartment(departmentName);
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Get courses by teacher ID")
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseResponse>> getCoursesByTeacherId(@PathVariable Long teacherId) {
        List<CourseResponse> courses = courseService.getCoursesByTeacherId(teacherId);
        return ResponseEntity.ok(courses);
    }
}
