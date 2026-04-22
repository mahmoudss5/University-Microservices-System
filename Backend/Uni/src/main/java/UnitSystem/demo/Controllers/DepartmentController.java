package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.DepartmentService;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentsDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Department", description = "Endpoints for department management")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "Get all departments")
    @GetMapping("/all")
    public ResponseEntity<List<DepartmentsDetails>> getAllDepartments() {
        List<DepartmentsDetails> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @Operation(summary = "Get department by ID")
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        DepartmentResponse department = departmentService.getDepartmentById(id);
        if (department != null) {
            return ResponseEntity.ok(department);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get department by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<DepartmentResponse> getDepartmentByName(@PathVariable String name) {
        DepartmentResponse department = departmentService.getDepartmentByName(name);
        if (department != null) {
            return ResponseEntity.ok(department);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a new department")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest departmentRequest) {
        DepartmentResponse department = departmentService.createDepartment(departmentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(department);
    }

    @Operation(summary = "Update an existing department")
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(@PathVariable Long id,
            @Valid @RequestBody DepartmentRequest departmentRequest) {
        DepartmentResponse department = departmentService.updateDepartment(id, departmentRequest);
        if (department != null) {
            return ResponseEntity.ok(department);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete a department")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
