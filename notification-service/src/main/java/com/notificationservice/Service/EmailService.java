package com.notificationservice.Service;

public interface EmailService {
    public String sendEmail(String to, String subject, String body);
}
