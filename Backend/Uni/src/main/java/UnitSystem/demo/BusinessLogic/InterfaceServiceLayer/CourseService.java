package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;

import java.util.List;

public interface CourseService {
    List<CourseResponse> getAllCourses();
    List<CourseResponse> getMostPopularCourses(int topN);
    CourseResponse getCourseById(Long courseId);
    CourseResponse createCourse(CourseRequest courseRequest);
    CourseResponse updateCourse( CourseRequest courseRequest, Long courseId);
    void deleteCourse(Long courseId);
    List<CourseResponse>getCoursesByDepartment(String departmentName);
     List<CourseResponse> getCoursesByTeacherId(Long teacherId);
    Teacher findCourseTeacher(Long courseId);
    Course getCourseEntityById(Long courseId);
  List<String> findStudentEmailsByCourseId(Long courseId);

}
