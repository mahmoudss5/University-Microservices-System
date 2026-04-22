package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.User.UserRequest;
import UnitSystem.demo.DataAccessLayer.Dto.User.UserResponse;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUserName())
                .email(user.getEmail())
                .active(user.getActive())
                .roles(user.getRoles())
                .build();
    }

    public User mapToUser(UserRequest userRequest) {
        return User.builder()
                .userName(userRequest.getUsername())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .active(true)
                .roles(new HashSet<>())
                .build();
    }
}
