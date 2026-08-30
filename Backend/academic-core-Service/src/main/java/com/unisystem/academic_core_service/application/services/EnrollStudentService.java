package com.unisystem.academic_core_service.application.services;

import com.unisystem.academic_core_service.application.port.in.EnrollStudentUseCase;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EnrollmentRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.application.port.out.CoursePrerequisiteRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.domain.events.StudentEnrollend;
import com.unisystem.academic_core_service.domain.exceptions.AlreadyEnrolledException;
import com.unisystem.academic_core_service.domain.exceptions.CourseNotFoundException;
import com.unisystem.academic_core_service.domain.exceptions.InvalidEnrollmentException;
import com.unisystem.academic_core_service.domain.exceptions.InvalidUserRoleException;
import com.unisystem.academic_core_service.domain.exceptions.PrerequisiteNotMetException;
import com.unisystem.academic_core_service.domain.exceptions.UserSnapshotNotFoundException;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.domain.model.Enrollment;
import com.unisystem.academic_core_service.domain.model.EnrollmentStatus;
import com.unisystem.academic_core_service.domain.model.UserRole;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;


public class EnrollStudentService  implements EnrollStudentUseCase {

     private final CourseRepositoryPort  courseRepositoryPort;
     private final EnrollmentRepositoryPort enrollmentRepositoryPort;
     private final EventPublisherPort eventPublisherPort;
     private final UserSnapshotRepositoryPort users;
     private final CoursePrerequisiteRepositoryPort prerequisites;

     public EnrollStudentService(CourseRepositoryPort courseRepositoryPort, EnrollmentRepositoryPort enrollmentRepositoryPort,
                                 EventPublisherPort eventPublisherPort, UserSnapshotRepositoryPort users,
                                 CoursePrerequisiteRepositoryPort prerequisites) {
        this.courseRepositoryPort = courseRepositoryPort;
        this.enrollmentRepositoryPort = enrollmentRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.users = users;
        this.prerequisites = prerequisites;
     }



    @Override
    @Transactional
    public  Enrollment enroll(EnrollCommand cmd) {
        var student = users.findById(cmd.studentId()).orElseThrow(() -> new UserSnapshotNotFoundException(cmd.studentId()));
        if (!student.active() || student.role() != UserRole.STUDENT) throw new InvalidUserRoleException(cmd.studentId(), UserRole.STUDENT);
        Course course=courseRepositoryPort.findByIdWithLock(cmd.courseId())
                .orElseThrow(()->new CourseNotFoundException(cmd.courseId()));

        List<Long> missingIds = prerequisites.findByCourseId(cmd.courseId()).stream()
                .map(p -> p.prerequisiteCourseId())
                .filter(id -> !enrollmentRepositoryPort.hasStudentCompletedCourse(cmd.studentId(), id)).toList();
        if (!missingIds.isEmpty()) {
            var courseById = courseRepositoryPort.findByIds(missingIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Course::getId, java.util.function.Function.identity()));
            var missing = missingIds.stream().map(id -> {
                Course required = courseById.get(id);
                return new PrerequisiteNotMetException.MissingPrerequisite(
                        id,
                        required == null ? null : required.getCourseCode(),
                        required == null ? null : required.getName());
            }).toList();
            throw new PrerequisiteNotMetException(cmd.studentId(), cmd.courseId(), missing);
        }

        Enrollment previous = enrollmentRepositoryPort.findByStudentIdAndCourseId(cmd.studentId(), cmd.courseId()).orElse(null);
        if (previous != null && previous.getStatus() != EnrollmentStatus.DROPPED) {
            throw new AlreadyEnrolledException(cmd.studentId(), cmd.courseId());
        }

        course.enrollStudent();
        courseRepositoryPort.save(course);

        Enrollment enrollment = previous == null ? Enrollment.create(cmd.studentId(), cmd.courseId()) : previous;
        if (previous != null) enrollment.reactivate();
        enrollment = enrollmentRepositoryPort.save(enrollment);

        StudentEnrollend event = new StudentEnrollend(
                cmd.studentId().toString(),
                cmd.courseId().toString(),
                course.getName() != null ? course.getName() : ""
        );
        eventPublisherPort.publishStudentEnrolled(event);
        return enrollment;
    }

    @Override
    @Transactional
    public void drop(Long studentId, Long courseId) {
        Course course=courseRepositoryPort.findById(courseId)
                .orElseThrow(()->new CourseNotFoundException(courseId));

      Enrollment enrollment=  enrollmentRepositoryPort.findByStudentIdAndCourseId(studentId, courseId)
              .orElseThrow(()-> new InvalidEnrollmentException("Enrollment not found"));

      if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) throw new InvalidEnrollmentException("Only active enrollment can be dropped");
      enrollment.drop();
      enrollmentRepositoryPort.save(enrollment);
      course.unenrollStudent();
      courseRepositoryPort.save(course);
      eventPublisherPort.publishStudentUnenrolled(new com.unisystem.academic_core_service.domain.events.StudentUnenrolledEvent(
              studentId.toString(), courseId.toString()));

    }

    @Override
    @Transactional
    public Enrollment complete(Long studentId, Long courseId, BigDecimal grade, boolean passed) {
        if (grade != null && (grade.signum() < 0 || grade.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new InvalidEnrollmentException("Grade must be between 0 and 100");
        }
        Enrollment enrollment = enrollmentRepositoryPort.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new InvalidEnrollmentException("Enrollment not found"));
        enrollment.complete(grade, passed);
        return enrollmentRepositoryPort.save(enrollment);
    }
}
