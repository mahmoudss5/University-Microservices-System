package com.unisystem.api_gateway.util;

import com.unisystem.api_gateway.dto.DashboardDtos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardUtilsTest {

    @Test
    void mergesAcademicCoursesIntoStudentProfile() {
        DashboardDtos.StudentProfileDto profile = new DashboardDtos.StudentProfileDto(
                1L, "STUDENT", "student", "student@uni.test", null, 30,
                List.of(), 0, 2025, "GOOD", List.of(), List.of());
        DashboardDtos.EnrolledCourseSummaryDto course = new DashboardDtos.EnrolledCourseSummaryDto(
                10L, 1L, "Student", 20L, "CS101", "Programming", "Teacher",
                3, null, null, null);

        DashboardDtos.StudentProfileDto result = DashboardUtils.mergeStudentCourses(profile, List.of(course));

        assertThat(result.enrolledCourses()).containsExactly(course);
        assertThat(result.enrolledCoursesCount()).isEqualTo(1);
    }

    @Test
    void supplementsTeacherCourseWithProfileMetadataAndNullSafeCounts() {
        DashboardDtos.TeacherCourseSummaryDto profileCourse = new DashboardDtos.TeacherCourseSummaryDto(
                20L, "Programming", null, "Computer Science", "teacher", 4, null, null);
        DashboardDtos.TeacherProfileDto profile = new DashboardDtos.TeacherProfileDto(
                2L, "TEACHER", "Teacher", "teacher@uni.test", null, "Computer Science",
                List.of(profileCourse), List.of(), List.of(), 1, 0);
        DashboardDtos.CourseDto course = new DashboardDtos.CourseDto(
                20L, "Programming", "CS101", "Intro", null, null, null,
                null, null, null, 3L, 2L);

        List<DashboardDtos.TeacherCourseSummaryDto> result =
                DashboardUtils.mapTeacherCourses(List.of(course), profile);

        assertThat(result).singleElement().satisfies(mapped -> {
            assertThat(mapped.departmentName()).isEqualTo("Computer Science");
            assertThat(mapped.creditHours()).isEqualTo(4);
            assertThat(mapped.maxStudents()).isZero();
            assertThat(mapped.enrolledStudents()).isZero();
        });
    }

    @Test
    void normalizesPrefixedRoles() {
        assertThat(DashboardUtils.normalizeRole("ROLE_STUDENT")).isEqualTo("student");
        assertThat(DashboardUtils.normalizeRole(null)).isEqualTo("unknown");
    }
}
