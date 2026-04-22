package UnitSystem.demo.DataAccessLayer.Dto.User;

import UnitSystem.demo.DataAccessLayer.Entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private  String username;
    private  String email;
    private  Boolean active;
    Set<Role> roles;
}
