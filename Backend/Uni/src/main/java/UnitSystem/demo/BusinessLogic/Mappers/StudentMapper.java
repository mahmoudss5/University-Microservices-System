package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Dto.EnrolledCourse.EnrolledCourseResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import UnitSystem.demo.DataAccessLayer.Entities.EnrolledCourse;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.Student;
import UnitSystem.demo.DataAccessLayer.Repositories.AnnouncementRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.UpcomingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StudentMapper {

    private final MappingUtils mappingUtils;
    private final PasswordEncoder passwordEncoder;
    private final AnnouncementRepository announcementRepository;
    private final UpcomingEventRepository upcomingEventRepository;

    public StudentResponse mapToStudentResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .userName(student.getUserName())
                .email(student.getEmail())
                .active(student.getActive())
                .createdAt(student.getCreatedAt())
                .gpa(student.getGpa())
                .enrollmentYear(student.getEnrollmentYear())
                .totalCredits(student.getTotalCredits())
                .roles(student.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .enrolledCoursesCount(student.getEnrolledCourses() != null ? student.getEnrolledCourses().size() : 0)
                .build();
    }

    public Student mapToStudent(StudentRequest studentRequest) {
        Set<Role> roles = mappingUtils.mapRoleNamesToRoles(studentRequest.getRoles());

        return Student.builder()
                .userName(studentRequest.getUserName())
                .email(studentRequest.getEmail())
                .password(passwordEncoder.encode(studentRequest.getPassword()))
                .active(studentRequest.getActive() != null ? studentRequest.getActive() : true)
                .gpa(studentRequest.getGpa())
                .enrollmentYear(studentRequest.getEnrollmentYear())
                .totalCredits(studentRequest.getTotalCredits())
                .roles(roles)
                .build();
    }

    public EnrolledCourseResponse mapToEnrolledCourseResponse(EnrolledCourse enrolledCourse) {
        return EnrolledCourseResponse.builder()
                .id(enrolledCourse.getId())
                .studentId(enrolledCourse.getStudent().getId())
                .studentName(enrolledCourse.getStudent() instanceof Student s ? s.getUserName() : "")
                .courseId(enrolledCourse.getCourse().getId())
                .courseCode(enrolledCourse.getCourse().getCourseCode())
                .credits((long) enrolledCourse.getCourse().getCredits())
                .teacherName(enrolledCourse.getCourse().getTeacher().getUserName())
                .courseName(enrolledCourse.getCourse().getName())
                .startDate(enrolledCourse.getCourse().getStartDate())
                .endDate(enrolledCourse.getCourse().getEndDate())
                .enrollmentDate(enrolledCourse.getEnrollmentDate())
                .build();
    }

    public List<AnnouncementResponse> mapAnnouncementsForStudent(Student student) {
        if (student.getEnrolledCourses() == null) {
            return Collections.emptyList();
        }
        return student.getEnrolledCourses().stream()
                .flatMap(enrolledCourse -> announcementRepository
                        .findByCourseId(enrolledCourse.getCourse().getId()).stream())
                .map(mappingUtils::mapToAnnouncementResponse)
                .collect(Collectors.toList());
    }

    public List<UpcomingEventResponse> mapUpcomingEventsForStudent(Long studentId) {
        return upcomingEventRepository.findByUserId(studentId).stream()
                .map(mappingUtils::mapToUpcomingEventResponse)
                .collect(Collectors.toList());
    }
}
