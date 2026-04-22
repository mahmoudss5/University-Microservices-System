package UnitSystem.demo.DataAccessLayer.Dto.Student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponse {
    private Long id;
    private String userName;
    private String email;
    private Boolean active;
    private LocalDateTime createdAt;
    private BigDecimal gpa;
    private int enrollmentYear;
    private int totalCredits;
    private Set<String> roles;
    private int enrolledCoursesCount;
}
