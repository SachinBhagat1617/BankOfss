package com.ofss.Customer.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class AccountCloseProducer {
    @Bean
    public NewTopic createAccountCloseTopic(){
        return new NewTopic("AccountCloseTopic",3,(short)1);
    }
}
