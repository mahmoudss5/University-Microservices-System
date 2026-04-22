package UnitSystem.demo.DataAccessLayer.Dto.Student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentRequest {
    private String userName;
    private String email;
    private String password;
    private Boolean active;
    private BigDecimal gpa;
    private int enrollmentYear;
    private int totalCredits;
    private Set<String> roles;
}
