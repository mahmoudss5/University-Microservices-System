package com.unisystem.academic_core_service.infrastructure.config;

import com.unisystem.academic_core_service.domain.model.ValueObjects.DepartmentsType;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.CourseEntity;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.DepartmentEntity;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.FeedbackEntity;
import com.unisystem.academic_core_service.infrastructure.adapters.out.iam.IamClient;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository.CourseJpaRepository;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository.DepartmentJpaRepository;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository.FeedbackJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Adds a small, repeatable demo dataset for local development.
 *
 * Every record has a natural key (department name, course code, or the
 * feedback comment) so restarting the service never creates duplicates.
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DepartmentJpaRepository departmentRepository;
    private final CourseJpaRepository courseRepository;
    private final FeedbackJpaRepository feedbackRepository;
    private final IamClient iamClient;

    @Value("${app.seed-data.enabled:true}")
    private boolean enabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            log.info("Demo data seeding is disabled");
            return;
        }

        Map<DepartmentsType, DepartmentEntity> departments = seedDepartments();
        List<Long> teacherIds = loadTeacherIds();
        if (teacherIds.isEmpty()) {
            log.warn("Skipping course and feedback seed because IAM teachers are not available yet");
            return;
        }
        List<CourseEntity> courses = seedCourses(departments, teacherIds);
        repairInvalidTeacherReferences(teacherIds);
        seedFeedback(courses, teacherIds);

        log.info("Demo data ready: {} departments, {} courses, {} feedback records",
                departmentRepository.count(), courseRepository.count(), feedbackRepository.count());
    }

    private Map<DepartmentsType, DepartmentEntity> seedDepartments() {
        Map<DepartmentsType, DepartmentEntity> departments = new EnumMap<>(DepartmentsType.class);

        for (DepartmentsType departmentType : DepartmentsType.values()) {
            String name = departmentType.name();
            DepartmentEntity department = departmentRepository.findByName(name)
                    .orElseGet(() -> departmentRepository.save(
                            DepartmentEntity.builder().name(name).build()));
            departments.put(departmentType, department);
        }
        return departments;
    }

    private List<Long> loadTeacherIds() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                List<Long> teacherIds = iamClient.getAllTeacherBasics().stream()
                        .map(IamClient.TeacherBasicResponse::getId)
                        .filter(id -> id != null)
                        .toList();
                if (!teacherIds.isEmpty()) {
                    return teacherIds;
                }
                log.warn("IAM returned no teachers while seeding academic data (attempt {}/5)", attempt);
            } catch (RuntimeException ex) {
                log.warn("IAM is not ready while seeding academic data (attempt {}/5): {}",
                        attempt, ex.getMessage());
            }

            if (attempt < 5) {
                try {
                    Thread.sleep(2_000L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return List.of();
                }
            }
        }
        return List.of();
    }

    private List<CourseEntity> seedCourses(Map<DepartmentsType, DepartmentEntity> departments,
                                           List<Long> teacherIds) {
        List<SeedCourse> seedCourses = List.of(
                new SeedCourse("Introduction to Programming", "CS101", "Learn programming fundamentals with Java and problem solving.", DepartmentsType.Computer_Science, 3, 40),
                new SeedCourse("Database Systems", "IS201", "Relational modelling, SQL, indexing, and transaction fundamentals.", DepartmentsType.Information_Systems, 3, 35),
                new SeedCourse("Software Architecture", "SE301", "Design principles and architectural patterns for maintainable systems.", DepartmentsType.Software_Engineering, 3, 30),
                new SeedCourse("Machine Learning Fundamentals", "AI201", "An introduction to supervised learning, evaluation, and feature engineering.", DepartmentsType.Artificial_Intelligence, 4, 30),
                new SeedCourse("Data Analysis with Python", "DS201", "Practical data cleaning, exploration, and visualisation using Python.", DepartmentsType.Data_Science, 3, 35)
        );

        return java.util.stream.IntStream.range(0, seedCourses.size())
                .mapToObj(index -> {
                    SeedCourse seed = seedCourses.get(index);
                    CourseEntity course = courseRepository.findByCourseCode(seed.courseCode())
                        .orElseGet(() -> courseRepository.save(CourseEntity.builder()
                                .name(seed.name())
                                .courseCode(seed.courseCode())
                                .description(seed.description())
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(4))
                                .credits(seed.credits())
                                .maxStudents(seed.maxStudents())
                                .enrolledCount(0)
                                .departmentId(departments.get(seed.department()).getId())
                                .teacherId(teacherIds.get(index % teacherIds.size()))
                                .build()));
                    if (!teacherIds.contains(course.getTeacherId())) {
                        course.setTeacherId(teacherIds.get(index % teacherIds.size()));
                        course = courseRepository.save(course);
                    }
                    return course;
                })
                .toList();
    }

    private void repairInvalidTeacherReferences(List<Long> teacherIds) {
        courseRepository.findAll().stream()
                .filter(course -> !teacherIds.contains(course.getTeacherId()))
                .forEach(course -> {
                    course.setTeacherId(teacherIds.get(0));
                    courseRepository.save(course);
                });
    }

    private void seedFeedback(List<CourseEntity> courses, List<Long> userIds) {
        for (int index = 0; index < courses.size(); index++) {
            CourseEntity course = courses.get(index);
            long userId = userIds.get(index % userIds.size());
            String comment = "[seed] A clear and useful course with engaging examples.";
            if (!feedbackRepository.existsByUserIdAndCourseIdAndComment(userId, course.getId(), comment)) {
                feedbackRepository.save(FeedbackEntity.builder()
                        .userId(userId)
                        .courseId(course.getId())
                        .comment(comment)
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build());
            }
        }
    }

    private record SeedCourse(
            String name,
            String courseCode,
            String description,
            DepartmentsType department,
            int credits,
            int maxStudents) {
    }
}
