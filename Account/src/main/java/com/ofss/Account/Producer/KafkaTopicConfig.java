package com.ofss.Account.Producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic createEmailNotificationTopic() {
        return new NewTopic("EmailNotificationTopic", 3, (short) 1);
    }
}