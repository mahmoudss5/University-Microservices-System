package UnitSystem.demo.Security.Oauth2;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.StudentService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UserService;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.RoleType;
import UnitSystem.demo.DataAccessLayer.Entities.Student;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.RoleRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import UnitSystem.demo.Security.Jwt.JwtService;
import UnitSystem.demo.Security.User.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final StudentService studentService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * GitHub OAuth2 success handler.
     * On first login: creates the user as a Student (default role) with a fabricated
     * email when GitHub doesn't expose one, then issues our own JWT.
     * On subsequent logins: finds the existing user and re-issues the JWT.
     *
     * GitHub profile attributes we care about:
     *   login  → used as userName (unique GitHub handle)
     *   email  → may be null if the user keeps it private → fabricate as login@github.com
     *   name   → display name, ignored for persistence
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String githubLogin = (String) attributes.get("login");
        String email       = (String) attributes.get("email");

        if (email == null) {
            email = githubLogin + "@github.com";
        }

        final String finalEmail    = email;
        final String finalUsername = githubLogin;

        User user = userService.findByEmail(finalEmail).orElseGet(() -> {
            // Avoid username collision — append _gh suffix when the GitHub login is taken
            String resolvedUsername = finalUsername;
            if (userRepository.existsByUserName(resolvedUsername)) {
                resolvedUsername = resolvedUsername + "_gh";
            }

            Role studentRole = roleRepository.findByName(RoleType.Student.name())
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name(RoleType.Student.name()).build()));

            Student newStudent = Student.builder()
                    .userName(resolvedUsername)
                    .email(finalEmail)
                    .password("")
                    .active(true)
                    .roles(Set.of(studentRole))
                    .gpa(BigDecimal.ZERO)
                    .enrollmentYear(0)
                    .totalCredits(0)
                    .build();

            studentService.saveUserAsStudent(newStudent);
            return newStudent;
        });

        String jwtToken = jwtService.generateToken(new SecurityUser(user));
        String targetUrl = frontendUrl + "/oauth2/redirect?token=" + jwtToken;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}