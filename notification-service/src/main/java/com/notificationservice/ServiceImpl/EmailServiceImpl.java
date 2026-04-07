package com.notificationservice.ServiceImpl;

import com.notificationservice.Service.EmailService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username}")
    private String senderMail;
    @Autowired
    private JavaMailSender mailSender;

    public String sendEmail(String to, String subject, String body) {
        log.info("Entered into the Email Service to send mail");
        try {
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom(new InternetAddress(senderMail,"Online Shopping"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.info("FAILED TO SEND EMAIL{}", e.getMessage());
            return "FAILED TO SEND EMAIL: " +e.getMessage();
        }
        return "SUCCESSFULLY EMAIL HAS BEEN SENT";
    }

}
