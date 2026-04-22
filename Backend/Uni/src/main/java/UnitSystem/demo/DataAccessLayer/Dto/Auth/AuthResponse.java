package UnitSystem.demo.DataAccessLayer.Dto.Auth;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String Username;
    private  String Token;
}
