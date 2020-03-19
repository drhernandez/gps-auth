package com.tesis.emails;

import com.tesis.users.User;

import java.util.List;

public interface EmailService {

    /**
     * Send recovery password email with token
     * @param receivers
     * @param recoveryToken
     */
    void sendRecoveryPasswordEmail(List<String> receivers, String recoveryToken);
    void sendWelcomePasswordEmail(List<String> receivers, String userName, String welcomeToken);
}
