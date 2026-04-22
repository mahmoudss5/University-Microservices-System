package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UserService;
import UnitSystem.demo.BusinessLogic.Mappers.UserMapper;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserRequest;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.RoleType;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.RoleRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import UnitSystem.demo.Security.Util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @CacheEvict(value = "usersCache", allEntries = true)
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating user: {}", userRequest.getUsername());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        if (userRepository.existsByUserName(userRequest.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        User user = userMapper.mapToUser(userRequest);

        // Assign default role (Student) if no roles specified
        Role defaultRole = roleRepository.findByName(RoleType.Student.name())
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(RoleType.Student.name())
                            .build();
                    return roleRepository.save(newRole);
                });

        user.setRoles(Set.of(defaultRole));
        userRepository.save(user);

        log.info("User created successfully: {}", user.getUserName());
        return userMapper.mapToUserResponse(user);
    }

    @Override
    @CacheEvict(value = "usersCache", allEntries = true)
    public UserResponse updateUser(UserRequest userRequest) {
        log.info("Updating user: {}", userRequest.getUsername());

        User existingUser = userRepository.findByUserName(userRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + userRequest.getUsername()));

        if (!existingUser.getEmail().equals(userRequest.getEmail())) {
            if (userRepository.existsByEmail(userRequest.getEmail())) {
                throw new RuntimeException("Email is already in use");
            }
            existingUser.setEmail(userRequest.getEmail());
        }

        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        userRepository.save(existingUser);
        log.info("User updated successfully: {}", existingUser.getUserName());
        return userMapper.mapToUserResponse(existingUser);
    }

    @Override
    @CacheEvict(value = "usersCache", allEntries = true)
    public UserResponse deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        UserResponse response = userMapper.mapToUserResponse(user);
        userRepository.delete(user);

        log.info("User deleted successfully: {}", user.getUserName());
        return response;
    }

    @Override
    @CacheEvict(value = "usersCache", allEntries = true)
    public UserResponse deActivateUser(Long userId) {
        log.info("Deactivating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully: {}", user.getUserName());
        return userMapper.mapToUserResponse(user);
    }

    @Override
    @CacheEvict(value = "usersCache", allEntries = true)
    public UserResponse deActivateCurrentUser() {
        log.info("Deactivating current user");

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("No authenticated user found");
        }

        return deActivateUser(currentUserId);
    }

    @Override
    @CacheEvict(value = "usersCache", allEntries = true)
    public void assignRoleToUser(Long userId, RoleType roleType) {
        log.info("Assigning role {} to user with ID: {}", roleType, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Role role = roleRepository.findByName(roleType.name())
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(roleType.name())
                            .build();
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>(user.getRoles());
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);
        log.info("Role {} assigned successfully to user: {}", roleType, user.getUserName());
    }

    @Override
    public void save(UserRequest userRequest) {
        log.info("Saving user: {}", userRequest.getUsername());

        if (userRepository.existsByUserName(userRequest.getUsername())) {
            updateUser(userRequest);
        } else {
            createUser(userRequest);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        log.info("Saving user entity: {}", user.getUserName());
        return userRepository.save(user);
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    @Override
    @Cacheable(value = "usersCache", key = "'allUsers'")
    public List<UserResponse> getAllUsers() {
        log.info("Retrieving all users");
        return userRepository.findAll().stream()
                .map(userMapper::mapToUserResponse)
                .collect(Collectors.toList());
    }

}
