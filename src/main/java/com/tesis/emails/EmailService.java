package com.tesis.emails;

import java.util.List;

public interface EmailService {

    /**
     * Send recovery password email with token
     * @param receivers
     * @param recoveryToken
     */
    void sendRecoveryPasswordEmail(List<String> receivers, String recoveryToken);
}
