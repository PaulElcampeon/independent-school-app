package com.paulo.independentschooldata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    @Value("${mail.author}")
    private String author;

    private final JavaMailSender mailSender;


//    @PostConstruct
//    public void init() {
//        System.out.println("Mail author set to: " + author);
//        sendSimpleEmail("Test Email", "paulthedev007@gmail.com", "This is a test email to verify mail configuration.");
//    }

    public void sendSimpleEmail(String schoolName, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(schoolName + " school information ");
        message.setText(
                "Hello I am currently researching potential schools for my child and would appreciate it if you could provide some information regarding your most recent exam results. Specifically, I am interested in the following:\n" +
                        "- The percentage of students who achieved Grade 5 or above in their GCSEs\n" +
                        "- The percentage of students who achieved C+ in their A-Level results\n\n" +
                        "Thank you in advance for your time and assistance.\n\n" +
                        "Kind regards,\nPaul"
        );

        message.setFrom(author);

        mailSender.send(message);
    }

    //    @Async("emailExecutor")
    public boolean sendSimpleEmail(String subject, String email, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);

            message.setFrom(author);

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Failed to process email with subject: {} due to : {}", subject, e.getMessage());
            return false;
        }
    }

    public void sendSimpleEmailToOurSelves(String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(author);
        message.setSubject(subject);
        message.setText(body);

        message.setFrom(author);

        mailSender.send(message);
    }
}