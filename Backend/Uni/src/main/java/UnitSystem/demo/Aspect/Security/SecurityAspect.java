package UnitSystem.demo.Aspect.Security;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.CourseService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UserService;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static UnitSystem.demo.Security.Util.SecurityUtils.getCurrentUserId;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityAspect {

    private final UserService userService;
    private final CourseService courseService;

    @Before("@annotation(UnitSystem.demo.Aspect.Security.TeachersOnly)")
    public void checkTeacherRole() {
        log.info("Checking if user has TEACHER role...");

        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("Current user ID is null. Access denied.");
        }

        User currentUser = userService.findUserById(userId);
        boolean isTeacher = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("TEACHER"));

        if (!isTeacher) {
            log.warn("Access denied for user {}. User does not have TEACHER role.", currentUser.getUserName());
            throw new RuntimeException("Access denied: You must be a teacher to access this resource.");
        }
        log.info("User {} has TEACHER role. Access granted.", currentUser.getUserName());
    }

    /**
     * Checks that the authenticated user is the teacher of the course being mutated.
     * Supports two method signatures:
     *   - updateCourse(CourseRequest, Long courseId)  → courseId is args[1]
     *   - deleteCourse(Long courseId)                 → courseId is args[0]
     */
    @Before("@annotation(UnitSystem.demo.Aspect.Security.CourseTeacherOnly)")
    public void checkCourseTeacher(JoinPoint joinPoint) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("Current user ID is null. Access denied.");
            throw new RuntimeException("Access denied: Current user ID is null.");
        }

        Long courseId = resolveCourseId(joinPoint.getArgs());
        Teacher courseTeacher = courseService.findCourseTeacher(courseId);

        if (courseTeacher == null || !courseTeacher.getId().equals(userId)) {
            log.warn("Access denied for user ID {}. Not the teacher of course ID {}.", userId, courseId);
            throw new RuntimeException("Access denied: You must be the teacher of this course.");
        }
        log.info("User ID {} confirmed as teacher of course ID {}. Access granted.", userId, courseId);
    }

    /**
     * Extracts the course ID from method arguments.
     * If the first argument is a Long it is used directly (single-arg delete methods).
     * Otherwise the second argument is expected to be the Long courseId (update methods).
     */
    private Long resolveCourseId(Object[] args) {
        if (args == null || args.length == 0) {
            throw new RuntimeException("Cannot determine course ID: no method arguments.");
        }
        if (args[0] instanceof Long id) {
            return id;
        }
        if (args.length > 1 && args[1] instanceof Long id) {
            return id;
        }
        throw new RuntimeException("Cannot determine course ID from method arguments.");
    }
}
