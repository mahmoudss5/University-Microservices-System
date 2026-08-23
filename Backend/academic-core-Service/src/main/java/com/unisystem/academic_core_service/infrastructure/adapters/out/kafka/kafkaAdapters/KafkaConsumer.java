package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.kafkaAdapters;

import com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.kafkaConifg.KafkaTopics;
import com.unisystem.academic_core_service.infrastructure.audit.AuditLogRecordingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

  private final AuditLogRecordingService auditLogRecordingService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = KafkaTopics.Student_Registered, groupId = "academic-core-group")
  public void listen(String message) {
      log.info("Received message on topic {}: {}", KafkaTopics.Student_Registered, message);
      try {
          Map<String, Object> event = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});
          Long userId = Long.valueOf(event.get("userId").toString());
          String username = (String) event.get("username");
          String email = (String) event.get("email");
          String role = (String) event.get("role");

          auditLogRecordingService.record(
                  userId,
                  username,
                  role,
                  "USER_REGISTERED",
                  "New user registered with email: " + email,
                  "N/A"
          );
          log.info("Audit log recorded for new user registration: {}", username);
      } catch (Exception e) {
          log.error("Failed to process Student_Registered event: {}", e.getMessage(), e);
      }
  }
}
