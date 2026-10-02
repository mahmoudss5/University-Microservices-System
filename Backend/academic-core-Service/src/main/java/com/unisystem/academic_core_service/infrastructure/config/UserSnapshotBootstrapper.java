package com.unisystem.academic_core_service.infrastructure.config;

import com.unisystem.academic_core_service.application.port.out.UserSnapshotRepositoryPort;
import com.unisystem.academic_core_service.domain.model.UserRole;
import com.unisystem.academic_core_service.domain.model.UserSnapshot;
import com.unisystem.academic_core_service.infrastructure.adapters.out.iam.IamClient;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Repairs the local read model when academic-core starts after IAM events were
 * already published. Kafka keeps the snapshot current, while this bootstrap
 * makes the read model recoverable after a new deployment or a missed event.
 */
@Slf4j
@Component
@Order(110)
@RequiredArgsConstructor
public class UserSnapshotBootstrapper implements CommandLineRunner {

    private final IamClient iamClient;
    private final UserSnapshotRepositoryPort snapshots;

    @Override
    public void run(String... args) {
        synchronizeSnapshots();
    }

    @Scheduled(
            initialDelayString = "${app.user-snapshot-bootstrap.initial-delay-ms:10000}",
            fixedDelayString = "${app.user-snapshot-bootstrap.fixed-delay-ms:60000}")
    public void retryBootstrap() {
        synchronizeSnapshots();
    }

    private void synchronizeSnapshots() {
        try {
            int synchronizedUsers = 0;
            LocalDateTime now = LocalDateTime.now();

            for (IamClient.StudentBasicResponse student : iamClient.getAllStudentBasics()) {
                if (student.getId() == null || student.getUsername() == null || student.getUsername().isBlank()) {
                    continue;
                }
                snapshots.save(new UserSnapshot(
                        student.getId(),
                        student.resolveUsername(),
                        UserRole.STUDENT,
                        student.isActive(),
                        now));
                synchronizedUsers++;
            }

            for (IamClient.TeacherBasicResponse teacher : iamClient.getAllTeacherBasics()) {
                if (teacher.getId() == null || teacher.getTeacherName() == null || teacher.getTeacherName().isBlank()) {
                    continue;
                }
                snapshots.save(new UserSnapshot(
                        teacher.getId(),
                        teacher.getTeacherName(),
                        UserRole.TEACHER,
                        teacher.isActive(),
                        now));
                synchronizedUsers++;
            }

            log.info("User snapshot bootstrap synchronized {} users", synchronizedUsers);
        } catch (RuntimeException exception) {
            // Kafka synchronization remains active; a temporary IAM outage
            // must not prevent academic-core from serving its own database.
            log.warn("Could not bootstrap user snapshots from IAM: {}", exception.getMessage());
        }
    }
}
