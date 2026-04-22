package UnitSystem.demo.DataAccessLayer.Dto.AuditLog;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditLogRequest {
    private Long userId;
    private String action;
    private String details;
    private String ipAddress;
}
