package UnitSystem.demo.DataAccessLayer.Dto.UserDetails;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailsRequest {
  private Long userId;
}
