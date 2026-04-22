package UnitSystem.demo.DataAccessLayer.Dto.EnrolledCourse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrolledCourseRequest {
    private Long studentId;
    private Long courseId;
}
