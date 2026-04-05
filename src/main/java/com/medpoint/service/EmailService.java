package com.medpoint.service;

public interface EmailService {
    void send(String to, String subject, String text);
    void sendDevOtp(String toEmail, String token);
}
