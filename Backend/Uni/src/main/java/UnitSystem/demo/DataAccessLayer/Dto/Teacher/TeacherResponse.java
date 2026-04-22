package UnitSystem.demo.DataAccessLayer.Dto.Teacher;

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
public class TeacherResponse {
    private Long id;
    private String userName;
    private String email;
    private Boolean active;
    private LocalDateTime createdAt;
    private String officeLocation;
    private BigDecimal salary;
    private Set<String> roles;
    private int coursesCount;
}
