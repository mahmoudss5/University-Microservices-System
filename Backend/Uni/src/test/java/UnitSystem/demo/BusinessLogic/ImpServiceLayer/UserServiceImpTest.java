package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.UserMapper;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserRequest;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.RoleType;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.RoleRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImpTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImp userServiceImp;

    private User user;
    private UserRequest userRequest;
    private UserResponse userResponse;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = Role.builder()
                .id(1L)
                .name(RoleType.Student.name())
                .build();

        user = User.builder()
                .id(1L)
                .userName("john_doe")
                .email("john@example.com")
                .password("hashed_password")
                .active(true)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();

        userRequest = UserRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("password123")
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .active(true)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();
    }

    @Test
    void createUser_validRequest_savesAndReturnsResponse() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUserName("john_doe")).thenReturn(false);
        when(userMapper.mapToUser(userRequest)).thenReturn(user);
        when(roleRepository.findByName(RoleType.Student.name())).thenReturn(Optional.of(studentRole));
        when(userMapper.mapToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userServiceImp.createUser(userRequest);

        assertNotNull(result);
        assertEquals("john_doe", result.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userServiceImp.createUser(userRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUserName("john_doe")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userServiceImp.createUser(userRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_roleNotFound_createsAndSavesNewRole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userMapper.mapToUser(userRequest)).thenReturn(user);
        when(roleRepository.findByName(RoleType.Student.name())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(studentRole);
        when(userMapper.mapToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userServiceImp.createUser(userRequest);

        assertNotNull(result);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void updateUser_existingUser_updatesAndReturnsResponse() {
        UserRequest updateRequest = UserRequest.builder()
                .username("john_doe")
                .email("newemail@example.com")
                .password("newPassword")
                .build();

        when(userRepository.findByUserName("john_doe")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("new_hashed");
        when(userMapper.mapToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userServiceImp.updateUser(updateRequest);

        assertNotNull(result);
        verify(userRepository).save(user);
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void updateUser_nonExistingUser_throwsException() {
        when(userRepository.findByUserName("john_doe")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userServiceImp.updateUser(userRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_emailAlreadyTaken_throwsException() {
        UserRequest updateRequest = UserRequest.builder()
                .username("john_doe")
                .email("taken@example.com")
                .build();

        when(userRepository.findByUserName("john_doe")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userServiceImp.updateUser(updateRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_existingId_deletesAndReturnsResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userServiceImp.deleteUser(1L);

        assertNotNull(result);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_nonExistingId_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userServiceImp.deleteUser(99L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deActivateUser_existingId_setsActiveToFalseAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userServiceImp.deActivateUser(1L);

        assertNotNull(result);
        assertFalse(user.getActive());
        verify(userRepository).save(user);
    }

    @Test
    void deActivateUser_nonExistingId_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userServiceImp.deActivateUser(99L));
    }

    @Test
    void assignRoleToUser_existingUserAndRole_addsRoleAndSaves() {
        Role adminRole = Role.builder().id(2L).name(RoleType.Admin.name()).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleType.Admin.name())).thenReturn(Optional.of(adminRole));

        userServiceImp.assignRoleToUser(1L, RoleType.Admin);

        assertTrue(user.getRoles().contains(adminRole));
        verify(userRepository).save(user);
    }

    @Test
    void assignRoleToUser_roleNotFound_createsRoleAndAssigns() {
        Role newRole = Role.builder().id(3L).name(RoleType.Teacher.name()).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleType.Teacher.name())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);

        userServiceImp.assignRoleToUser(1L, RoleType.Teacher);

        verify(roleRepository).save(any(Role.class));
        verify(userRepository).save(user);
    }

    @Test
    void getAllUsers_returnsListOfUserResponses() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.mapToUserResponse(user)).thenReturn(userResponse);

        List<UserResponse> result = userServiceImp.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("john_doe", result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void findByEmail_existingEmail_returnsUser() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userServiceImp.findByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUserName());
    }

    @Test
    void findByEmail_nonExistingEmail_returnsEmptyOptional() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userServiceImp.findByEmail("unknown@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void findUserById_existingId_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userServiceImp.findUserById(1L);

        assertNotNull(result);
        assertEquals("john_doe", result.getUserName());
    }

    @Test
    void findUserById_nonExistingId_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userServiceImp.findUserById(99L));
    }
}
