package com.tesis.emails;

import java.util.List;

public interface EmailService {

    /**
     * Send recovery password email with token
     * @param receivers
     * @param recoveryToken
     */
    void sendRecoveryPasswordEmail(List<String> receivers, String recoveryToken);

    /**
     * Send welcome email with token to set password
     * @param receivers
     * @param userName
     * @param welcomeToken
     */
    void sendWelcomePasswordEmail(List<String> receivers, String userName, String welcomeToken);
}
