package UnitSystem.demo.DataAccessLayer.Dto.Course;


import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseRequest {
    private String name;
    private String description;
    private String courseCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String departmentName;
    private String teacherUserName;
    private int creditHours;
    private int maxStudents;

}
