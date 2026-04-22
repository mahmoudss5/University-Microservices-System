package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.StudentService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.TeacherService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.AuthService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UserService;
import UnitSystem.demo.BusinessLogic.Mappers.AuthMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Auth.AuthRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Auth.AuthResponse;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserRequest;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.RoleType;
import UnitSystem.demo.DataAccessLayer.Entities.Student;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.RoleRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import UnitSystem.demo.ExcHandler.Entites.AuthError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final AuthMapper authMapper;

    @Override
    public AuthResponse register(UserRequest userRequest) {
        log.info("Registering User: {}", userRequest);

        validateUniqueCredentials(userRequest);

        boolean isTeacher = userRequest.getTeacherCode() != null;

        Role role = resolveRole(isTeacher);

        User user = isTeacher ? buildAndSaveTeacher(userRequest, role)
                : buildAndSaveStudent(userRequest, role);

        return authMapper.mapToAuthResponse(user);
    }

    private void validateUniqueCredentials(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new AuthError("Email is already in use");
        }
        if (userRepository.existsByUserName(userRequest.getUsername())) {
            throw new AuthError("Username is already in use");
        }
    }

    private Role resolveRole(boolean isTeacher) {
        String roleName = isTeacher ? RoleType.Teacher.name() : RoleType.Student.name();
        return roleRepository.findByName(roleName)
                .orElse(Role.builder().name(roleName).build());
    }

    private User buildAndSaveTeacher(UserRequest userRequest, Role role) {
        User teacher = Teacher.builder()
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .userName(userRequest.getUsername())
                .roles(Set.of(role))
                .active(true)
                .officeLocation("")
                .salary(BigDecimal.ZERO)
                .build();
        teacherService.saveUserASTeacher(teacher);
        return teacher;
    }

    private User buildAndSaveStudent(UserRequest userRequest, Role role) {
        User student = Student.builder()
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .userName(userRequest.getUsername())
                .roles(Set.of(role))
                .active(true)
                .gpa(BigDecimal.ZERO)
                .enrollmentYear(0)
                .totalCredits(0)
                .build();
        studentService.saveUserAsStudent(student);
        return student;
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for email: {}", request.getEmail());
            throw new AuthError("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        log.info("User {} authenticated successfully", user.getEmail());

        return authMapper.mapToAuthResponse(user);
    }
}
