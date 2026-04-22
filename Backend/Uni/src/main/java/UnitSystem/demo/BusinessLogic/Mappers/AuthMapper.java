package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.Auth.AuthResponse;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.Security.Jwt.JwtService;
import UnitSystem.demo.Security.User.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMapper {

    private final JwtService jwtService;

    public AuthResponse mapToAuthResponse(User user) {
        log.info("Mapping User to AuthResponse: {}", user);
        return AuthResponse.builder()
                .Username(user.getUserName())
                .Token(jwtService.generateToken(new SecurityUser(user)))
                .build();
    }
}
