package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.AuditLogService;
import UnitSystem.demo.BusinessLogic.Mappers.AuditLogMapper;
import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogRequest;
import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogResponse;
import UnitSystem.demo.DataAccessLayer.Entities.AuditLog;
import UnitSystem.demo.DataAccessLayer.Repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImp implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Cacheable(value = "auditLogsCache", key = "'allAuditLogs'")
    public List<AuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAll().stream()
                .map(auditLogMapper::mapToAuditLogResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "auditLogsCache", key = "'auditLogsByUser:' + #userName")
    public List<AuditLogResponse> getAuditLogsByUserName(String userName) {
        return auditLogRepository.findAllByUserUserName(userName).stream()
                .map(auditLogMapper::mapToAuditLogResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "auditLogsCache", key = "'auditLogsByAction:' + #action")
    public List<AuditLogResponse> getAuditLogsByAction(String action) {
        return auditLogRepository.findAllByAction(action).stream()
                .map(auditLogMapper::mapToAuditLogResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "auditLogsCache", key = "'auditLogsByActionAndUser:' + #action + ':' + #userName")
    public List<AuditLogResponse> getAuditLogsByActionAndUserName(String action, String userName) {
        return auditLogRepository.findAllByActionAndUserUserName(action, userName).stream()
                .map(auditLogMapper::mapToAuditLogResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "auditLogsCache", key = "'auditLogById:' + #auditLogId")
    public AuditLogResponse getAuditLogById(Long auditLogId) {
        return auditLogRepository.findById(auditLogId)
                .map(auditLogMapper::mapToAuditLogResponse)
                .orElse(null);
    }

    @Override
    @CacheEvict(value = "auditLogsCache", allEntries = true)
    public AuditLogResponse createAuditLog(AuditLogRequest auditLogRequest) {
        AuditLog auditLog = auditLogMapper.mapToAuditLog(auditLogRequest);
        auditLogRepository.save(auditLog);
        return auditLogMapper.mapToAuditLogResponse(auditLog);
    }

    @Override
    @CacheEvict(value = "auditLogsCache", allEntries = true)
    public void deleteAuditLog(Long auditLogId) {
        auditLogRepository.deleteById(auditLogId);
    }
}
