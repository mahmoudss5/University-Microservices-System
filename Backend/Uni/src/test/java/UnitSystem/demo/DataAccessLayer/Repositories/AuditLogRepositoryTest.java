package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.AuditLog;
import UnitSystem.demo.DataAccessLayer.Entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AuditLogRepositoryTest {


    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User testUser2;
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserName("testuser");
        testUser.setEmail("test@gmail.com");
        testUser.setPassword("password");
        testUser.setActive(true);
        testUser2 = new User();
        testUser2.setUserName("testuser2");
        testUser2.setEmail("test2@gami.com");
        testUser2.setPassword("password");
        testUser2.setActive(true);
        userRepository.save(testUser);
        userRepository.save(testUser2);
    }

    @Test
    void testFindAllByUserUserName() {
        // Given
        AuditLog log1 = new AuditLog();
        log1.setUser(testUser);
        log1.setAction("LOGIN");
        auditLogRepository.save(log1);

        AuditLog log2 = new AuditLog();
        log2.setUser(testUser);
        log2.setAction("LOGOUT");
        auditLogRepository.save(log2);

        // When
        var logs = auditLogRepository.findAllByUserUserName("testuser");

        // Then
        assertEquals(2, logs.size());
    }
    @Test
    void testFindAllByUserEmail() {
        // Given
        AuditLog log1 = new AuditLog();
        log1.setUser(testUser);
        log1.setAction("LOGIN");
        auditLogRepository.save(log1);

        AuditLog log2 = new AuditLog();
        log2.setUser(testUser);
        log2.setAction("LOGOUT");
        auditLogRepository.save(log2);

        // When
        var logs = auditLogRepository.findAllByUserEmail("test@gmail.com");

        // Then
        assertEquals(2, logs.size());
    }
    @Test
    void testFindAllByAction() {
        // Given
        AuditLog log1 = new AuditLog();
        log1.setUser(testUser);
        log1.setAction("LOGIN");
        auditLogRepository.save(log1);

        AuditLog log2 = new AuditLog();
        log2.setUser(testUser2);
        log2.setAction("LOGIN");
        auditLogRepository.save(log2);

        // When
        var logs = auditLogRepository.findAllByAction("LOGIN");

        // Then
        assertEquals(2, logs.size());
    }

    @Test
    void testFindAllByActionAndUserUserName() {
        // Given
        AuditLog log1 = new AuditLog();
        log1.setUser(testUser);
        log1.setAction("LOGIN");
        auditLogRepository.save(log1);

        AuditLog log2 = new AuditLog();
        log2.setUser(testUser);
        log2.setAction("LOGOUT");
        auditLogRepository.save(log2);

        AuditLog log3 = new AuditLog();
        log3.setUser(testUser2);
        log3.setAction("LOGIN");
        auditLogRepository.save(log3);

        // When
        var logs = auditLogRepository.findAllByActionAndUserUserName("LOGIN", "testuser");

        // Then
        assertEquals(1, logs.size());
        assertEquals("testuser", logs.get(0).getUser().getUserName());
    }

}