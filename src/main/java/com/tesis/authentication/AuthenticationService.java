package com.tesis.authentication;

import com.tesis.users.User;

import java.util.Optional;

public interface AuthenticationService {

    /**
     * Validate token
     * @param accessToken
     * @return
     */
    boolean validateToken(AccessToken accessToken);

    /**
     * Generate new access token from given user
     * @param user
     * @return
     */
    AccessToken createAccessToken(User user);

    /**
     * Validate if client credentials match for a registered user and generates the access_token
     * @param credentialsBody
     * @return
     */
    AccessToken login(ClientCredentialsBody credentialsBody);
}
