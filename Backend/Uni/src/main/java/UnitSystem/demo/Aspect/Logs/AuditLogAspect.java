package UnitSystem.demo.Aspect.Logs;


import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.AuditLogService;
import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogRequest;
import UnitSystem.demo.DataAccessLayer.Repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static UnitSystem.demo.Security.Util.SecurityUtils.getCurrentUserId;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @AfterReturning(pointcut = "@annotation(UnitSystem.demo.Aspect.Logs.AuditLog)", returning = "result")
    public void logAfterMethodExecution(JoinPoint joinPoint, Object result) {
        log.info("Method executed successfully with result: {}", result);

        String methodName = joinPoint.getSignature().getName();
        String details = "Executed method: " + methodName + " with result: " + result;

        Long userId=getCurrentUserId();
       if (userId==null) {
           throw  new RuntimeException("Current user id is null");
       }
        AuditLogRequest auditLogRequest = AuditLogRequest.builder()
                .userId(userId)
                .action(methodName)
                .details(details)
                .ipAddress("N/A") // You can enhance this to capture the actual IP address
                .build();
        auditLogService.createAuditLog(auditLogRequest);
    }
}
