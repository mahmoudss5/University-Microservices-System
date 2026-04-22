package UnitSystem.demo.Controllers;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.AuditLogService;
import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogRequest;
import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Log", description = "Endpoints for audit log management")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Get all audit logs")
    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        List<AuditLogResponse> auditLogs = auditLogService.getAllAuditLogs();
        return ResponseEntity.ok(auditLogs);
    }

    @Operation(summary = "Get audit logs by username")
    @GetMapping("/username/{userName}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUserName(@PathVariable String userName) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByUserName(userName);
        return ResponseEntity.ok(auditLogs);
    }

    @Operation(summary = "Get audit logs by action")
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByAction(@PathVariable String action) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByAction(action);
        return ResponseEntity.ok(auditLogs);
    }

    @Operation(summary = "Get audit logs by action and username")
    @GetMapping("/action/{action}/username/{userName}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByActionAndUserName(
            @PathVariable String action,
            @PathVariable String userName) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByActionAndUserName(action, userName);
        return ResponseEntity.ok(auditLogs);
    }

    @Operation(summary = "Get audit log by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getAuditLogById(@PathVariable Long id) {
        AuditLogResponse auditLog = auditLogService.getAuditLogById(id);
        if (auditLog != null) {
            return ResponseEntity.ok(auditLog);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a new audit log")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuditLogResponse> createAuditLog(@Valid @RequestBody AuditLogRequest auditLogRequest) {
        AuditLogResponse auditLog = auditLogService.createAuditLog(auditLogRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(auditLog);
    }

    @Operation(summary = "Delete an audit log")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteAuditLog(@PathVariable Long id) {
        auditLogService.deleteAuditLog(id);
        return ResponseEntity.noContent().build();
    }
}
