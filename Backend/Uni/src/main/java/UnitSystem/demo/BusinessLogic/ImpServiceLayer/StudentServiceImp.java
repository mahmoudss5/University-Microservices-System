package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.StudentService;
import UnitSystem.demo.BusinessLogic.Mappers.MappingUtils;
import UnitSystem.demo.BusinessLogic.Mappers.StudentMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Announcement.AnnouncementResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UpcomingEvent.UpcomingEventResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.StudentDetailsResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.UserDetailsRequest;
import UnitSystem.demo.DataAccessLayer.Dto.EnrolledCourse.EnrolledCourseResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Student;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImp implements StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentMapper studentMapper;
   private final MappingUtils mappingUtils;

    @Override
    @Cacheable(value = "studentsCache", key = "'allStudents'")
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(studentMapper::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "studentsCache", key = "'studentById:' + #studentId")
    public StudentResponse getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .map(studentMapper::mapToStudentResponse)
                .orElse(null);
    }

    @Override
    @Cacheable(value = "studentsCache", key = "'studentByUserName:' + #userName")
    public StudentResponse getStudentByUserName(String userName) {
        Student student = studentRepository.findByUserName(userName);
        return student != null ? studentMapper.mapToStudentResponse(student) : null;
    }

    @Override
    @CacheEvict(value = "studentsCache", allEntries = true)
    public StudentResponse createStudent(StudentRequest studentRequest) {
        Student student = studentMapper.mapToStudent(studentRequest);
        studentRepository.save(student);
        return studentMapper.mapToStudentResponse(student);
    }

    @Override
    @CacheEvict(value = "studentsCache", allEntries = true)
    public StudentResponse updateStudent(Long studentId, StudentRequest studentRequest) {
        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        existingStudent.setUserName(studentRequest.getUserName());
        existingStudent.setEmail(studentRequest.getEmail());
        if (studentRequest.getPassword() != null && !studentRequest.getPassword().isEmpty()) {
            existingStudent.setPassword(passwordEncoder.encode(studentRequest.getPassword()));
        }
        existingStudent.setActive(studentRequest.getActive());
        existingStudent.setGpa(studentRequest.getGpa());
        existingStudent.setEnrollmentYear(studentRequest.getEnrollmentYear());
        existingStudent.setTotalCredits(studentRequest.getTotalCredits());

        if (studentRequest.getRoles() != null) {
            existingStudent.setRoles(mappingUtils.mapRoleNamesToRoles(studentRequest.getRoles()));
        }

        studentRepository.save(existingStudent);
        return studentMapper.mapToStudentResponse(existingStudent);
    }

    @Override
    public void saveUserAsStudent(User user) {
        Student student = (Student) user;
        studentRepository.save(student);
    }

    @Override
    @CacheEvict(value = "studentsCache", allEntries = true)
    public void deleteStudent(Long studentId) {
        studentRepository.deleteById(studentId);
    }

    private String calculateAcademicStanding(BigDecimal gpa) {
        if (gpa == null)
            return "N/A";
        double g = gpa.doubleValue();
        if (g >= 3.5)
            return "Excellent";
        if (g >= 3.0)
            return "Very Good";
        if (g >= 2.5)
            return "Good";
        if (g >= 2.0)
            return "Satisfactory";
        return "Probation";
    }

    @Override
    @Cacheable(value = "studentsCache", key = "'studentDetails:' + #userDetailsRequest.userId")
    public StudentDetailsResponse getStudentDetails(UserDetailsRequest userDetailsRequest) {
        Student student = studentRepository.findById(userDetailsRequest.getUserId())
                .orElseThrow(
                        () -> new RuntimeException("Student not found with ID: " + userDetailsRequest.getUserId()));

        Set<EnrolledCourseResponse> enrolledCourses = student.getEnrolledCourses() != null
                ? student.getEnrolledCourses().stream()
                        .map(studentMapper::mapToEnrolledCourseResponse)
                        .collect(Collectors.toSet())
                : Collections.emptySet();

        // Get all announcements from student's enrolled courses
        List<AnnouncementResponse> announcements = studentMapper.mapAnnouncementsForStudent(student);

        // Get upcoming events for the student
        List<UpcomingEventResponse> upcomingEvents = studentMapper.mapUpcomingEventsForStudent(student.getId());

        return StudentDetailsResponse.builder()
                .id(student.getId())
                .username(student.getUserName())
                .email(student.getEmail())
                .gpa(student.getGpa())
                .enrollmentYear(student.getEnrollmentYear())
                .totalCredits((long) student.getTotalCredits())
                .enrolledCourses(enrolledCourses)
                .enrolledCoursesCount(enrolledCourses.size())
                .academicStanding(calculateAcademicStanding(student.getGpa()))
                .announcements(announcements)
                .upcomingEvents(upcomingEvents)
                .build();
    }
}
