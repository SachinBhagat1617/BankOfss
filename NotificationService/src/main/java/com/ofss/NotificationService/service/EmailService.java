
package com.ofss.NotificationService.service;

import com.ofss.NotificationService.dto.EmailEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper; // Import for manual parsing
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    @KafkaListener(topics = "EmailNotificationTopic", groupId = "email-service")
    public void sendEmail(EmailEventDTO emailEvent) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailEvent.getTo());
        message.setSubject(emailEvent.getSubject());
        message.setText(emailEvent.getBody());

        mailSender.send(message);
        System.out.println("Email sent to: " + emailEvent.getTo());
    }

}
