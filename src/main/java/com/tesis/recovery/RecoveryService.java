package com.tesis.recovery;

import com.tesis.users.User;

public interface RecoveryService {

    /**
     * Generate a recovery token base on user email and send and email with the token
     * @param email
     * @return
     */
    RecoveryToken createToken(String email);

    /**
     * Send and welcome email with the token
     * @param user
     * @return
     */
    RecoveryToken createWelcomeToken(User user);

    /**
     * Validate is token is not expired
     * @param token
     * @return
     */
    boolean validateToken(String token);

    /**
     * This method allow the owner of the token to change their user password
     * @param token
     * @param rawPassword
     */
    void changeUserPassword(String token, String rawPassword);
}
