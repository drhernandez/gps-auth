package com.tesis.recovery;

public interface RecoveryService {

    /**
     * Generate a recovery token base on user email
     * @param email
     * @return
     */
    RecoveryToken createToken(String email);

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
