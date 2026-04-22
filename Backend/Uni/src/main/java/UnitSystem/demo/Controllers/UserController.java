package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UserService;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserRequest;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserResponse;
import UnitSystem.demo.DataAccessLayer.Entities.RoleType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Endpoints for user management")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Create a new user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse user = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Update an existing user")
    @PutMapping
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse user = userService.updateUser(userRequest);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete a user by ID")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deactivate a user by ID")
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<UserResponse> deActivateUser(@PathVariable Long userId) {
        UserResponse user = userService.deActivateUser(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Deactivate current authenticated user")
    @PatchMapping("/deactivate-current")
    public ResponseEntity<UserResponse> deActivateCurrentUser() {
        UserResponse user = userService.deActivateCurrentUser();
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Assign a role to a user")
    @PostMapping("/{userId}/roles/{role}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable RoleType role) {
        userService.assignRoleToUser(userId, role);
        return ResponseEntity.ok().build();
    }
}
