package UnitSystem.demo.Security.WebSocket;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.CourseService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.EnrolledCourseService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.StudentService;
import UnitSystem.demo.DataAccessLayer.Entities.Student;
import UnitSystem.demo.Security.User.CustomUserDetailsService;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import lombok.RequiredArgsConstructor;
import UnitSystem.demo.Security.Jwt.JwtService;

import java.security.Principal;

@RequiredArgsConstructor
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final EnrolledCourseService enrolledCourseService;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);

                if (username != null) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username, null, userDetails.getAuthorities());
                        accessor.setUser(authentication);
                    }
                }
            }
        }
        else if (StompCommand.SUBSCRIBE.equals(command)) {
            String destination = accessor.getDestination();

            if (destination != null && destination.startsWith("/topic/course/")) {

                Principal user = accessor.getUser();
                if (user == null) {
                    throw new RuntimeException("Unauthorized: No authenticated user found");
                }

                String userEmail = user.getName();

                String[] parts = destination.split("/");
                if (parts.length >= 4) {
                    try {
                        Long courseId = Long.valueOf(parts[3]);
                        if (!enrolledCourseService.isStudentEnrolledInCourse(userEmail, courseId)) {
                            throw new RuntimeException("Unauthorized: User is not enrolled in course " + courseId);
                        }
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Bad Request: Invalid course ID format");
                    }
                }
            }
        }

        return message;
    }
}
