package com.unisystem.academic_core_service.application.services;

import com.unisystem.academic_core_service.application.port.in.CreateCourseUseCase;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.domain.exceptions.InvalidUserRoleException;
import com.unisystem.academic_core_service.domain.exceptions.UserSnapshotNotFoundException;
import com.unisystem.academic_core_service.domain.model.UserRole;
import com.unisystem.academic_core_service.domain.events.CourseCreatedEvent;
import com.unisystem.academic_core_service.domain.exceptions.DuplicateCourseException;
import com.unisystem.academic_core_service.domain.model.Course;

import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

public class CreateCourseService implements CreateCourseUseCase {

    private final CourseRepositoryPort courseRepository;
    private final EventPublisherPort eventPublisher;
    private final UserSnapshotRepositoryPort users;

    public CreateCourseService(CourseRepositoryPort courseRepository, EventPublisherPort eventPublisher, UserSnapshotRepositoryPort users) {
        this.courseRepository = courseRepository;
        this.eventPublisher = eventPublisher;
        this.users = users;
    }

    @Override
    @Transactional
    public Course create(CreateCourseCommand cmd) {
        var teacher = users.findById(cmd.teacherId()).orElseThrow(() -> new UserSnapshotNotFoundException(cmd.teacherId()));
        if (!teacher.active() || teacher.role() != UserRole.TEACHER) throw new InvalidUserRoleException(cmd.teacherId(), UserRole.TEACHER);
        Boolean exists = courseRepository.existsByCourseCode(cmd.courseCode());
        if (exists) {
            throw new DuplicateCourseException(cmd.courseCode());
        }

        if (cmd.startDate() != null && cmd.endDate() != null && cmd.endDate().isBefore(cmd.startDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Course course = new Course();
        course.setName(cmd.name());
        course.setCourseCode(cmd.courseCode());
        course.setDescription(cmd.description());
        course.setMaxStudents(cmd.maxStudents());
        course.setCredits(cmd.credits());
        course.setDepartmentId(cmd.departmentId());
        course.setTeacherId(cmd.teacherId());
        course.setStartDate(cmd.startDate());
        course.setEndDate(cmd.endDate());
        course.setEnrolledCount(0);
        course.setCreatedAt(LocalDate.from(java.time.LocalDateTime.now()));
        Course savedCourse = courseRepository.save(course);
        CourseCreatedEvent event = new CourseCreatedEvent(savedCourse.getId().toString(), savedCourse.getName(), savedCourse.getCourseCode(),savedCourse.getCreatedAt());
        eventPublisher.publishCourseCreated(event);
        return savedCourse;
    }
}
