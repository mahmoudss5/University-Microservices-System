package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.TeacherService;
import UnitSystem.demo.BusinessLogic.Mappers.MappingUtils;
import UnitSystem.demo.BusinessLogic.Mappers.TeacherMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.TeacherDetailsResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.UserDetailsRequest;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImp implements TeacherService {

        private final MappingUtils mappingUtils;
        private final TeacherRepository teacherRepository;
        private final PasswordEncoder passwordEncoder;
        private final TeacherMapper teacherMapper;

        @Override
        public List<TeacherResponse> getAllTeachers() {
                return teacherRepository.findAll().stream()
                                .map(teacherMapper::mapToTeacherResponse)
                                .toList();
        }

        @Override
        @Cacheable(value = "teachersCache", key = "'teacherById:' + #teacherId")
        public TeacherResponse getTeacherById(Long teacherId) {
                return teacherRepository.findById(teacherId)
                                .map(teacherMapper::mapToTeacherResponse)
                                .orElse(null);
        }

        @Override
        @Cacheable(value = "teachersCache", key = "'teacherByUserName:' + #userName")
        public TeacherResponse getTeacherByUserName(String userName) {
                Teacher teacher = teacherRepository.findByUserName(userName);
                return teacher != null ? teacherMapper.mapToTeacherResponse(teacher) : null;
        }

        @Override
        @CacheEvict(value = "teachersCache", allEntries = true)
        public TeacherResponse createTeacher(TeacherRequest teacherRequest) {
                Teacher teacher = teacherMapper.mapToTeacher(teacherRequest);
                teacherRepository.save(teacher);
                return teacherMapper.mapToTeacherResponse(teacher);
        }

        @Override
        @CacheEvict(value = "teachersCache", allEntries = true)
        public TeacherResponse updateTeacher(Long teacherId, TeacherRequest teacherRequest) {
                Teacher existingTeacher = teacherRepository.findById(teacherId)
                                .orElseThrow(() -> new RuntimeException("Teacher not found"));

                existingTeacher.setUserName(teacherRequest.getUserName());
                existingTeacher.setEmail(teacherRequest.getEmail());
                if (teacherRequest.getPassword() != null && !teacherRequest.getPassword().isEmpty()) {
                        existingTeacher.setPassword(passwordEncoder.encode(teacherRequest.getPassword()));
                }
                existingTeacher.setActive(teacherRequest.getActive());
                existingTeacher.setOfficeLocation(teacherRequest.getOfficeLocation());
                existingTeacher.setSalary(teacherRequest.getSalary());

                if (teacherRequest.getRoles() != null) {
                        existingTeacher.setRoles(mappingUtils.mapRoleNamesToRoles(teacherRequest.getRoles()));
                }

                teacherRepository.save(existingTeacher);
                return teacherMapper.mapToTeacherResponse(existingTeacher);
        }

        @Override
        public void saveUserASTeacher(User user) {
                Teacher teacher = (Teacher) user;
                teacherRepository.save(teacher);
        }

        @Override
        @CacheEvict(value = "teachersCache", allEntries = true)
        public void deleteTeacher(Long teacherId) {
                teacherRepository.deleteById(teacherId);
        }

        @Override
        @Cacheable(value = "teachersCache", key = "'teacherDetails:' + #userDetailsRequest.userId")
        public TeacherDetailsResponse getTeacherDetails(UserDetailsRequest userDetailsRequest) {
                Teacher teacher = teacherRepository.findById(userDetailsRequest.getUserId())
                                .orElseThrow(
                                                () -> new RuntimeException("Teacher not found with ID: "
                                                                + userDetailsRequest.getUserId()));

                Set<CourseResponse> courses = teacher.getCourses() != null
                                ? teacher.getCourses().stream()
                                                .map(teacherMapper::mapToCourseResponse)
                                                .collect(Collectors.toSet())
                                : Collections.emptySet();

                // Get all announcements from teacher's courses
                List<AnnouncementResponse> announcements = teacher.getCourses() != null
                                ? teacherMapper.mapAnnouncementsForTeacher(teacher)
                                : Collections.emptyList();

                // Get upcoming events for the teacher
                List<UpcomingEventResponse> upcomingEvents = teacherMapper.mapUpcomingEventsForTeacher(teacher.getId());

                // Calculate total number of students across all courses
                Long numberOfStudents = teacher.getCourses() != null
                                ? teacher.getCourses().stream()
                                                .mapToLong(course -> course.getCourseEnrollments() != null
                                                                ? course.getCourseEnrollments().size() : 0)
                                                .sum()
                                : 0L;

                // Get department from teacher's courses (first course's department)
                String department = teacher.getCourses() != null && !teacher.getCourses().isEmpty()
                                ? teacher.getCourses().iterator().next().getDepartment().getName()
                                : null;

                return TeacherDetailsResponse.builder()
                                .teacherId(teacher.getId())
                                .name(teacher.getUserName())
                                .email(teacher.getEmail())
                                .department(department)
                                .salary(teacher.getSalary())
                                .roles(teacher.getRoles().stream()
                                                .map(Role::getName)
                                                .collect(Collectors.toSet()))
                                .courses(courses)
                                .announcements(announcements)
                                .upcomingEvents(upcomingEvents)
                                .coursesCount(courses.size())
                                .numberOfStudents(numberOfStudents)
                                .build();
        }
}
