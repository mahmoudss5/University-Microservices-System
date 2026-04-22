package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserRequest;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserResponse;
import UnitSystem.demo.DataAccessLayer.Entities.RoleType;
import UnitSystem.demo.DataAccessLayer.Entities.User;

import java.util.List;
import java.util.Optional;

public interface UserService {



    UserResponse createUser(UserRequest userRequest);

    UserResponse updateUser(UserRequest userRequest);

    UserResponse deleteUser(Long userId);

    UserResponse deActivateUser(Long userId);

    UserResponse deActivateCurrentUser();
    List<UserResponse> getAllUsers();

    void assignRoleToUser(Long userId, RoleType role);
    
    void save(UserRequest userRequest);
    Optional<User> findByEmail(String email);
    User save(User user);
    User findUserById(Long id);

}
