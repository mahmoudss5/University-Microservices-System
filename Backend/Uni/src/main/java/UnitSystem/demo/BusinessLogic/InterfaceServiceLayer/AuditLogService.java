package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogRequest;
import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogResponse;

import java.util.List;

public interface AuditLogService {
    List<AuditLogResponse> getAllAuditLogs();

    List<AuditLogResponse> getAuditLogsByUserName(String userName);

    List<AuditLogResponse> getAuditLogsByAction(String action);

    List<AuditLogResponse> getAuditLogsByActionAndUserName(String action, String userName);

    AuditLogResponse getAuditLogById(Long auditLogId);

    AuditLogResponse createAuditLog(AuditLogRequest auditLogRequest);

    void deleteAuditLog(Long auditLogId);
}
