package com.tesis.emails;

import com.tesis.emails.templates.EmailTemplate;
import com.tesis.emails.templates.RecoveryEmailTemplate;
import com.tesis.emails.templates.WelcomeEmailTemplate;
import com.tesis.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultEmailService implements EmailService {

    private final SendGridClient mailClient;
    private final String senderMail;
    private final String recoveryUrl;

    @Autowired
    public DefaultEmailService(SendGridClient mailClient, @Value("${email.sender-address}") String senderMail, @Value("${email.recovery.url}") String recoveryUrl) {
        this.mailClient = mailClient;
        this.senderMail = senderMail;
        this.recoveryUrl = recoveryUrl;
    }

    @Override
    public void sendRecoveryPasswordEmail(List<String> receivers, String recoveryToken) {

        EmailTemplate recoveryTemplate = RecoveryEmailTemplate.builder()
                .senderMail(senderMail)
                .receivers(receivers)
                .recoveryLink(recoveryUrl + recoveryToken)
                .build();

        mailClient.sendMail(recoveryTemplate.get());
    }

    @Override
    public void sendWelcomePasswordEmail(List<String> receivers, String userName) {
        EmailTemplate welcomeTemplate = WelcomeEmailTemplate.builder()
                .senderMail(senderMail)
                .receivers(receivers)
                .userName(userName)
                .build();

        mailClient.sendMail(welcomeTemplate.get());
    }

}
