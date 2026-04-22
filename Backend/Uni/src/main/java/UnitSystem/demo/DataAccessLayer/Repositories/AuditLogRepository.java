package UnitSystem.demo.DataAccessLayer.Repositories;
import UnitSystem.demo.DataAccessLayer.Entities.AuditLog;
import UnitSystem.demo.DataAccessLayer.Entities.AuditLogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
   List<AuditLog> findAllByUserUserName(String username);
   List<AuditLog> findAllByUserEmail(String email);
   List<AuditLog> findAllByAction(String action);
   List<AuditLog> findAllByActionAndUserUserName(String action, String username);
}
