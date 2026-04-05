package com.medpoint.service.impl;

import com.medpoint.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@medpoint.com}")
    private String fromAddress;

    @Override
    public void send(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }

    @Override
    public void sendDevOtp(String toEmail, String token) {
        String text = "Your MedPoint Developer Portal access token is:\n\n"
                + "    " + token + "\n\n"
                + "This token expires in 15 minutes. Do not share it with anyone.\n\n"
                + "If you did not request this token, please ignore this email.\n\n"
                + "— MedPoint System";
        send(toEmail, "MedPoint Dev Portal — Your Access Token", text);
    }
}
