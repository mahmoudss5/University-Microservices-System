package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Repositories.DepartmentRepository;
import UnitSystem.demo.DataAccessLayer.Repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;

    public CourseResponse mapToCourseResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .creditHours(course.getCredits())
                .description(course.getDescription())
                .courseCode(course.getCourseCode())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .enrolledStudents(course.getCourseEnrollments().size())
                .maxStudents(course.getCapacity())
                .departmentName(course.getDepartment().getName())
                .teacherUserName(course.getTeacher().getUserName())
                .build();
    }

    public Course mapToCourse(CourseRequest courseRequest) {
        return Course.builder()
                .name(courseRequest.getName())
                .description(courseRequest.getDescription())
                .courseCode(courseRequest.getCourseCode())
                .startDate(courseRequest.getStartDate())
                .endDate(courseRequest.getEndDate())
                .credits(courseRequest.getCreditHours())
                .capacity(courseRequest.getMaxStudents())
                .department(departmentRepository.findByName(courseRequest.getDepartmentName()))
                .teacher(teacherRepository.findByUserName(courseRequest.getTeacherUserName()))
                .build();
    }
}
