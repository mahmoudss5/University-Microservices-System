package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Entities.Course;

public interface CourseService {
    String getCourseName(Long id);

    void saveCourse(Course course);
}
