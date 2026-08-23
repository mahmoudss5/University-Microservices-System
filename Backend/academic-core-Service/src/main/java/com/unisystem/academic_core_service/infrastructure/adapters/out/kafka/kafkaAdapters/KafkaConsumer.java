package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.kafkaAdapters;

import com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.kafkaConifg.KafkaTopics;
import com.unisystem.academic_core_service.infrastructure.audit.AuditLogRecordingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

  private final AuditLogRecordingService auditLogRecordingService;


  @KafkaListener(topics = KafkaTopics.Student_Registered)
  public void listen(String message) {

  }
}
