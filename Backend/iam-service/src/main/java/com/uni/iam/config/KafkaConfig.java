package com.uni.iam.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic studentRegisteredTopic() {
        return TopicBuilder.name("student-registered")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean public NewTopic userRegisteredTopic() { return topic("user-registered-v1"); }
    @Bean public NewTopic userUpdatedTopic() { return topic("user-updated-v1"); }
    @Bean public NewTopic userDeactivatedTopic() { return topic("user-deactivated-v1"); }
    @Bean public NewTopic userDeletedTopic() { return topic("user-deleted-v1"); }
    @Bean public NewTopic securityAuditTopic() { return topic("security-audit-events.v1"); }

    private NewTopic topic(String name) { return TopicBuilder.name(name).partitions(1).replicas(1).build(); }
}
