package UnitSystem.demo.DataAccessLayer.Dto.EnrolledCourse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrolledCourseResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String teacherName;
    private Long credits;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime enrollmentDate;
}
