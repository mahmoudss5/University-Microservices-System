package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.AuthService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UserService;
import UnitSystem.demo.DataAccessLayer.Dto.Auth.AuthRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Auth.AuthResponse;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

private final AuthService authService;

    @Operation(summary = "Authenticate user and generate JWT token")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.login(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Register a new user and generate JWT token")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRequest Request) {
        log.info("Register a new user and generate JWT token");
        AuthResponse authResponse = authService.register(Request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

}
