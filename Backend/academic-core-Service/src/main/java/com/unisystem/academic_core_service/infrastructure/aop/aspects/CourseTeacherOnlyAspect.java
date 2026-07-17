package com.unisystem.academic_core_service.infrastructure.aop.aspects;

import com.unisystem.academic_core_service.domain.application.port.in.GetCoursesQuery;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.infrastructure.aop.annotations.CourseTeacherOnly;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.OptionalInt;

@Aspect
@Component
@Order(1)
public class CourseTeacherOnlyAspect {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String TEACHER_ROLE = "TEACHER";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String DEFAULT_COURSE_ID_PARAM = "courseId";

    private static final String MISSING_AUTHENTICATED_USER = "Missing authenticated user";
    private static final String TEACHER_OR_ADMIN_REQUIRED =
            "Only users with TEACHER or ADMIN role can perform this action";
    private static final String COURSE_NOT_FOUND = "Course not found";
    private static final String COURSE_OWNER_REQUIRED =
            "Only the assigned teacher may perform this action on this course";

    private final GetCoursesQuery getCoursesQuery;

    public CourseTeacherOnlyAspect(GetCoursesQuery getCoursesQuery) {
        this.getCoursesQuery = getCoursesQuery;
    }

    @Before("@annotation(ann)")
    public void enforceCourseTeacher(JoinPoint joinPoint, CourseTeacherOnly ann) {
        Authentication authentication = requireAuthentication();
        if (hasRole(authentication, ADMIN_ROLE)) {
            return;
        }

        requireTeacherRole(authentication);

        if (!ann.requireCourseOwnership()) {
            return;
        }

        assertCourseOwner(authentication, resolveCourseId(joinPoint, ann));
    }

    private void assertCourseOwner(Authentication authentication, Long courseId) {
        long userId = authenticatedUserId(authentication);
        Course course = findCourse(courseId);
        if (!Objects.equals(course.getTeacherId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, COURSE_OWNER_REQUIRED);
        }
    }

    private Course findCourse(Long courseId) {
        return getCoursesQuery.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, COURSE_NOT_FOUND));
    }

    private static Authentication requireAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MISSING_AUTHENTICATED_USER);
        }
        return authentication;
    }

    private static void requireTeacherRole(Authentication authentication) {
        if (!hasRole(authentication, TEACHER_ROLE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, TEACHER_OR_ADMIN_REQUIRED);
        }
    }

    private static boolean hasRole(Authentication authentication, String role) {
        String roleAuthority = ROLE_PREFIX + role;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority())
                        || roleAuthority.equals(authority.getAuthority()));
    }

    private static long authenticatedUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, MISSING_AUTHENTICATED_USER);
        }
        return parsePositiveLong(principal.toString(), "authenticated user id");
    }

    private Long resolveCourseId(JoinPoint joinPoint, CourseTeacherOnly ann) {
        MethodInvocation invocation = MethodInvocation.from(joinPoint);

        if (hasBodyParameter(ann)) {
            return resolveCourseIdFromBody(invocation, ann);
        }

        return resolveCourseIdFromMethodParameter(invocation, configuredCourseIdParameter(ann));
    }

    private static boolean hasBodyParameter(CourseTeacherOnly ann) {
        return ann.bodyParam() != null && !ann.bodyParam().isBlank();
    }

    private static Long resolveCourseIdFromBody(MethodInvocation invocation, CourseTeacherOnly ann) {
        int bodyIndex = requireParameterIndex(invocation, ann.bodyParam());
        Object body = invocation.args()[bodyIndex];
        Long courseId = readLongProperty(body, configuredBodyField(ann));

        if (courseId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course id missing in request body");
        }

        return courseId;
    }

    private static Long resolveCourseIdFromMethodParameter(MethodInvocation invocation, String parameterName) {
        int parameterIndex = findAnnotatedParameterIndex(invocation, parameterName)
                .orElseGet(() -> requireParameterIndex(invocation, parameterName));
        return requireLong(invocation.args()[parameterIndex], "Invalid course id");
    }

    private static OptionalInt findAnnotatedParameterIndex(MethodInvocation invocation, String parameterName) {
        Parameter[] parameters = invocation.parameters();
        for (int i = 0; i < parameters.length; i++) {
            String annotationName = requestParameterName(invocation, i);
            if (parameterName.equals(annotationName)) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    private static String requestParameterName(MethodInvocation invocation, int parameterIndex) {
        Parameter parameter = invocation.parameters()[parameterIndex];
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (pathVariable != null) {
            return firstPresent(pathVariable.value(), pathVariable.name(), invocation.parameterName(parameterIndex));
        }

        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null) {
            return firstPresent(requestParam.value(), requestParam.name(), invocation.parameterName(parameterIndex));
        }

        return null;
    }

    private static int requireParameterIndex(MethodInvocation invocation, String parameterName) {
        for (int i = 0; i < invocation.parameters().length; i++) {
            if (parameterName.equals(invocation.parameterName(i))) {
                return i;
            }
        }

        throw new IllegalStateException(
                "Cannot resolve course id: no parameter '" + parameterName + "' on "
                        + invocation.method().getName());
    }

    private static String configuredCourseIdParameter(CourseTeacherOnly ann) {
        return firstPresent(ann.param(), DEFAULT_COURSE_ID_PARAM);
    }

    private static String configuredBodyField(CourseTeacherOnly ann) {
        return firstPresent(ann.bodyField(), DEFAULT_COURSE_ID_PARAM);
    }

    private static String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static Long readLongProperty(Object body, String field) {
        if (body == null) {
            return null;
        }

        try {
            Method accessor = findAccessor(body.getClass(), field);
            return coerceToLong(accessor.invoke(body));
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course id unreadable from request body", e);
        }
    }

    private static Method findAccessor(Class<?> type, String field) throws NoSuchMethodException {
        try {
            return type.getMethod(field);
        } catch (NoSuchMethodException ignored) {
            return type.getMethod(getterName(field));
        }
    }

    private static String getterName(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    private static Long requireLong(Object value, String message) {
        Long result = coerceToLong(value);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return result;
    }

    private static Long coerceToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long longValue) {
            return requirePositive(longValue, "course id");
        }
        if (value instanceof Number number) {
            return requirePositive(number.longValue(), "course id");
        }
        String rawValue = value.toString().trim();
        if (rawValue.isEmpty()) {
            return null;
        }
        return parsePositiveLong(rawValue, "course id");
    }

    private static long parsePositiveLong(String value, String source) {
        try {
            long parsedValue = Long.parseLong(value);
            if (parsedValue <= 0) {
                throw new NumberFormatException(source + " must be positive");
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + source, e);
        }
    }

    private static long requirePositive(long value, String source) {
        if (value <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + source);
        }
        return value;
    }

    private record MethodInvocation(
            Method method,
            Object[] args,
            Parameter[] parameters,
            String[] parameterNames
    ) {

        private static MethodInvocation from(JoinPoint joinPoint) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            return new MethodInvocation(
                    method,
                    joinPoint.getArgs(),
                    method.getParameters(),
                    signature.getParameterNames());
        }

        private String parameterName(int index) {
            if (parameterNames != null && index < parameterNames.length) {
                return parameterNames[index];
            }
            return parameters[index].getName();
        }
    }
}
