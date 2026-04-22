package UnitSystem.demo.DataAccessLayer.Dto.User;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private  String username;
    private String email;
    private String password;
    private String teacherCode;
}
