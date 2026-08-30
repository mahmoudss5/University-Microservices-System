package com.unisystem.academic_core_service.infrastructure.adapters.out.kafka.kafkaConifg;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic studentEnrolledTopic() {
        return TopicBuilder.name(KafkaTopics.STUDENT_ENROLLED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic courseCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.COURSE_CREATED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic announcementCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.ANNOUNCEMENT_CREATED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationPushTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_PUSH)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userRegisteredTopic() { return TopicBuilder.name("user-registered-v1").partitions(1).replicas(1).build(); }
    @Bean
    public NewTopic userUpdatedTopic() { return TopicBuilder.name("user-updated-v1").partitions(1).replicas(1).build(); }
    @Bean
    public NewTopic userDeactivatedTopic() { return TopicBuilder.name("user-deactivated-v1").partitions(1).replicas(1).build(); }
    @Bean
    public NewTopic userDeletedTopic() { return TopicBuilder.name("user-deleted-v1").partitions(1).replicas(1).build(); }

}
