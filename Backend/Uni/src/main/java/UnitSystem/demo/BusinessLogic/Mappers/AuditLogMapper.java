package UnitSystem.demo.BusinessLogic.Mappers;

import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogRequest;
import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogResponse;
import UnitSystem.demo.DataAccessLayer.Entities.AuditLog;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import UnitSystem.demo.DataAccessLayer.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditLogMapper {

    private final UserRepository userRepository;

    public AuditLogResponse mapToAuditLogResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .userName(auditLog.getUser() != null ? auditLog.getUser().getUserName() : null)
                .action(auditLog.getAction())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    public AuditLog mapToAuditLog(AuditLogRequest auditLogRequest) {
        User user = null;
        if (auditLogRequest.getUserId() != null) {
            user = userRepository.findById(auditLogRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        return AuditLog.builder()
                .user(user)
                .action(auditLogRequest.getAction())
                .details(auditLogRequest.getDetails())
                .ipAddress(auditLogRequest.getIpAddress())
                .build();
    }
}
