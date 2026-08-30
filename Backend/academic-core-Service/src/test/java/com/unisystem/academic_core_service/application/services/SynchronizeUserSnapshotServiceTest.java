package com.unisystem.academic_core_service.application.services;

import static org.mockito.Mockito.*;
import com.unisystem.academic_core_service.application.port.in.SynchronizeUserSnapshotUseCase.Command;
import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.domain.model.UserRole;
import org.junit.jupiter.api.Test;

class SynchronizeUserSnapshotServiceTest {
    private final UserSnapshotRepositoryPort repository = mock(UserSnapshotRepositoryPort.class);
    private final SynchronizeUserSnapshotService service = new SynchronizeUserSnapshotService(repository);

    @Test void upsertsSnapshotByUserId() {
        service.synchronize(new Command(10L, "sara", UserRole.STUDENT, true, false));
        verify(repository).save(argThat(value -> value.userId().equals(10L) && value.active()));
    }

    @Test void deletesSnapshotForDeletedEvent() {
        service.synchronize(new Command(10L, "sara", UserRole.STUDENT, false, true));
        verify(repository).deleteById(10L);
        verify(repository, never()).save(any());
    }
}
