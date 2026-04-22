package UnitSystem.demo.DataAccessLayer.Dto.Teacher;

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
public class TeacherRequest {
    private String userName;
    private String email;
    private String password;
    private Boolean active;
    private String officeLocation;
    private BigDecimal salary;
    private Set<String> roles;
}
