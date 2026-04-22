package UnitSystem.demo.DataAccessLayer.Dto.AuditLog;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String action;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}
