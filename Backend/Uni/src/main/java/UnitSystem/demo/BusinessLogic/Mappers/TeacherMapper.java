package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
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
public class TeacherMapper {

    private final MappingUtils mappingUtils;
    private final PasswordEncoder passwordEncoder;
    private final AnnouncementRepository announcementRepository;
    private final UpcomingEventRepository upcomingEventRepository;

    public TeacherResponse mapToTeacherResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .userName(teacher.getUserName())
                .email(teacher.getEmail())
                .active(teacher.getActive())
                .createdAt(teacher.getCreatedAt())
                .officeLocation(teacher.getOfficeLocation())
                .salary(teacher.getSalary())
                .roles(teacher.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .coursesCount(teacher.getCourses() != null ? teacher.getCourses().size() : 0)
                .build();
    }

    public Teacher mapToTeacher(TeacherRequest teacherRequest) {
        Set<Role> roles = mappingUtils.mapRoleNamesToRoles(teacherRequest.getRoles());

        return Teacher.builder()
                .userName(teacherRequest.getUserName())
                .email(teacherRequest.getEmail())
                .password(passwordEncoder.encode(teacherRequest.getPassword()))
                .active(teacherRequest.getActive() != null ? teacherRequest.getActive() : true)
                .officeLocation(teacherRequest.getOfficeLocation())
                .salary(teacherRequest.getSalary())
                .roles(roles)
                .build();
    }

    public CourseResponse mapToCourseResponse(UnitSystem.demo.DataAccessLayer.Entities.Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .departmentName(course.getDepartment() != null ? course.getDepartment().getName() : null)
                .teacherUserName(course.getTeacher() != null ? course.getTeacher().getUserName() : null)
                .creditHours(course.getCredits())
                .maxStudents(course.getCapacity())
                .enrolledStudents(course.getCourseEnrollments() != null ? course.getCourseEnrollments().size() : 0)
                .build();
    }

    public List<AnnouncementResponse> mapAnnouncementsForTeacher(Teacher teacher) {
        if (teacher.getCourses() == null) {
            return Collections.emptyList();
        }
        return teacher.getCourses().stream()
                .flatMap(course -> announcementRepository.findByCourseId(course.getId()).stream())
                .map(mappingUtils::mapToAnnouncementResponse)
                .collect(Collectors.toList());
    }

    public List<UpcomingEventResponse> mapUpcomingEventsForTeacher(Long teacherId) {
        return upcomingEventRepository.findByUserId(teacherId).stream()
                .map(mappingUtils::mapToUpcomingEventResponse)
                .collect(Collectors.toList());
    }
}
