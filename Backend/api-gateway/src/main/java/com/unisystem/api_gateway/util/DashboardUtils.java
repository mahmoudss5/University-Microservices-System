package com.unisystem.api_gateway.util;

import com.unisystem.api_gateway.dto.DashboardDtos;

import java.util.List;
import java.util.Locale;

public final class DashboardUtils {

    private DashboardUtils() {
    }

    public static DashboardDtos.StudentProfileDto mergeStudentCourses(
            DashboardDtos.StudentProfileDto profile,
            List<DashboardDtos.EnrolledCourseSummaryDto> courses) {
        List<DashboardDtos.EnrolledCourseSummaryDto> safeCourses = courses == null ? List.of() : courses;
        return new DashboardDtos.StudentProfileDto(
                profile.id(), profile.role(), profile.username(), profile.email(), profile.gpa(),
                profile.totalCredits(), safeCourses, safeCourses.size(), profile.enrollmentYear(),
                profile.academicStanding(), profile.announcements(), profile.upcomingEvents());
    }

    public static List<DashboardDtos.TeacherCourseSummaryDto> mapTeacherCourses(
            List<DashboardDtos.CourseDto> courses,
            DashboardDtos.TeacherProfileDto profile) {
        if (courses == null) {
            return List.of();
        }
        return courses.stream()
                .filter(course -> course != null)
                .map(course -> toTeacherCourseSummary(course, profile))
                .toList();
    }

    public static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "unknown";
        }
        return role.replaceFirst("(?i)^ROLE_", "").toLowerCase(Locale.ROOT);
    }

    private static DashboardDtos.TeacherCourseSummaryDto toTeacherCourseSummary(
            DashboardDtos.CourseDto course,
            DashboardDtos.TeacherProfileDto profile) {
        DashboardDtos.TeacherCourseSummaryDto profileCourse = findProfileCourse(profile, course.id());
        return new DashboardDtos.TeacherCourseSummaryDto(
                course.id(), course.name(), course.description(),
                profileCourse == null ? null : profileCourse.departmentName(),
                profileCourse == null ? null : profileCourse.teacherUserName(),
                profileCourse != null && profileCourse.creditHours() != null
                        ? profileCourse.creditHours() : safeInt(course.credits()),
                safeInt(course.maxStudents()), safeInt(course.enrolledCount()));
    }

    private static DashboardDtos.TeacherCourseSummaryDto findProfileCourse(
            DashboardDtos.TeacherProfileDto profile,
            Long courseId) {
        if (profile == null || profile.courses() == null || courseId == null) {
            return null;
        }
        return profile.courses().stream()
                .filter(course -> course != null && courseId.equals(course.id()))
                .findFirst()
                .orElse(null);
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
