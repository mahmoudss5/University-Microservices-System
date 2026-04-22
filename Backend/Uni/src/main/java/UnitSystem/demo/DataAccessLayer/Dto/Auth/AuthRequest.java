package UnitSystem.demo.DataAccessLayer.Dto.Auth;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private  String email;
    private  String password;

}
