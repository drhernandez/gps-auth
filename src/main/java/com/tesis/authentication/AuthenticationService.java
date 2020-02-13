package com.tesis.authentication;

public interface AuthenticationService {

    /**
     * Validate if client credentials match for a registered user and generates the access_token
     * @param credentialsBody
     * @return
     */
    AccessToken login(ClientCredentialsBody credentialsBody);

    /**
     * Find token asociated to user and remove it from db
     * @param accessToken
     */
    void logout(String accessToken);
}
