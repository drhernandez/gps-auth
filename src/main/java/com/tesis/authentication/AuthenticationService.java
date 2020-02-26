package com.tesis.authentication;

import com.tesis.exceptions.ForbiddenException;
import com.tesis.exceptions.UnauthorizedException;
import com.tesis.privileges.Privilege;

import java.util.List;

public interface AuthenticationService {

    /**
     * Validate if client credentials match for a registered user and generates the access_token
     * @param credentialsBody
     * @return
     */
    AccessToken login(ClientCredentialsBody credentialsBody) throws UnauthorizedException;

    /**
     * Find token asociated to user and remove it from db
     * @param accessToken
     */
    void logout(String accessToken);

    /**
     * Validate if the token has the list of privileges
     * @param token
     * @param privileges
     * @throws UnauthorizedException if token is invalid
     * @throws ForbiddenException if any privileges is missing in the token
     */
    void validatePrivilegesOnAccessToken(String token, List<String> privileges) throws UnauthorizedException, ForbiddenException;
}
