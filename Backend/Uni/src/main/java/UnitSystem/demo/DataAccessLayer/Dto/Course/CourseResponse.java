package UnitSystem.demo.DataAccessLayer.Dto.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private String departmentName;
    private String teacherUserName;
    private String courseCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private int creditHours;
    private int maxStudents;
    private int enrolledStudents;
}
