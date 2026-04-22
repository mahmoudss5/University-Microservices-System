package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.Mappers.AuditLogMapper;
import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogRequest;
import UnitSystem.demo.DataAccessLayer.Dto.AuditLog.AuditLogResponse;
import UnitSystem.demo.DataAccessLayer.Entities.AuditLog;
import UnitSystem.demo.DataAccessLayer.Repositories.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImpTest {

    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private AuditLogMapper auditLogMapper;
    @InjectMocks
    private AuditLogServiceImp auditLogServiceImp;

    private AuditLog auditLog;
    private AuditLogRequest auditLogRequest;
    private AuditLogResponse auditLogResponse;

    @BeforeEach
    void setUp() {
        auditLog = AuditLog.builder()
                .id(1L)
                .action("LOGIN")
                .details("User logged in from 192.168.1.1")
                .ipAddress("192.168.1.1")
                .build();

        auditLogRequest = AuditLogRequest.builder()
                .userId(1L)
                .action("LOGIN")
                .details("User logged in from 192.168.1.1")
                .ipAddress("192.168.1.1")
                .build();

        auditLogResponse = AuditLogResponse.builder()
                .id(1L)
                .userId(1L)
                .userName("john_doe")
                .action("LOGIN")
                .details("User logged in from 192.168.1.1")
                .ipAddress("192.168.1.1")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllAuditLogs_returnsListOfAuditLogs() {
        when(auditLogRepository.findAll()).thenReturn(List.of(auditLog));
        when(auditLogMapper.mapToAuditLogResponse(auditLog)).thenReturn(auditLogResponse);

        List<AuditLogResponse> result = auditLogServiceImp.getAllAuditLogs();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LOGIN", result.get(0).getAction());
        verify(auditLogRepository).findAll();
    }

    @Test
    void getAuditLogsByUserName_existingUser_returnsLogs() {
        when(auditLogRepository.findAllByUserUserName("john_doe")).thenReturn(List.of(auditLog));
        when(auditLogMapper.mapToAuditLogResponse(auditLog)).thenReturn(auditLogResponse);

        List<AuditLogResponse> result = auditLogServiceImp.getAuditLogsByUserName("john_doe");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository).findAllByUserUserName("john_doe");
    }

    @Test
    void getAuditLogsByUserName_nonExistingUser_returnsEmptyList() {
        when(auditLogRepository.findAllByUserUserName("unknown")).thenReturn(List.of());

        List<AuditLogResponse> result = auditLogServiceImp.getAuditLogsByUserName("unknown");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAuditLogsByAction_existingAction_returnsLogs() {
        when(auditLogRepository.findAllByAction("LOGIN")).thenReturn(List.of(auditLog));
        when(auditLogMapper.mapToAuditLogResponse(auditLog)).thenReturn(auditLogResponse);

        List<AuditLogResponse> result = auditLogServiceImp.getAuditLogsByAction("LOGIN");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository).findAllByAction("LOGIN");
    }

    @Test
    void getAuditLogsByActionAndUserName_returnsMatchingLogs() {
        when(auditLogRepository.findAllByActionAndUserUserName("LOGIN", "john_doe"))
                .thenReturn(List.of(auditLog));
        when(auditLogMapper.mapToAuditLogResponse(auditLog)).thenReturn(auditLogResponse);

        List<AuditLogResponse> result =
                auditLogServiceImp.getAuditLogsByActionAndUserName("LOGIN", "john_doe");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository).findAllByActionAndUserUserName("LOGIN", "john_doe");
    }

    @Test
    void getAuditLogById_existingId_returnsResponse() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(auditLog));
        when(auditLogMapper.mapToAuditLogResponse(auditLog)).thenReturn(auditLogResponse);

        AuditLogResponse result = auditLogServiceImp.getAuditLogById(1L);

        assertNotNull(result);
        assertEquals("LOGIN", result.getAction());
    }

    @Test
    void getAuditLogById_nonExistingId_returnsNull() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());

        AuditLogResponse result = auditLogServiceImp.getAuditLogById(99L);

        assertNull(result);
    }

    @Test
    void createAuditLog_validRequest_savesAndReturnsResponse() {
        when(auditLogMapper.mapToAuditLog(auditLogRequest)).thenReturn(auditLog);
        when(auditLogMapper.mapToAuditLogResponse(auditLog)).thenReturn(auditLogResponse);

        AuditLogResponse result = auditLogServiceImp.createAuditLog(auditLogRequest);

        assertNotNull(result);
        assertEquals("LOGIN", result.getAction());
        verify(auditLogRepository).save(auditLog);
    }

    @Test
    void deleteAuditLog_callsRepositoryDeleteById() {
        doNothing().when(auditLogRepository).deleteById(1L);

        auditLogServiceImp.deleteAuditLog(1L);

        verify(auditLogRepository).deleteById(1L);
    }
}
