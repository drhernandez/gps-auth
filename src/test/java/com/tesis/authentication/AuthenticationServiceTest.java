package com.tesis.authentication;

import com.google.common.collect.Sets;
import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.ForbiddenException;
import com.tesis.exceptions.InternalServerErrorException;
import com.tesis.exceptions.UnauthorizedException;
import com.tesis.privileges.Privilege;
import com.tesis.roles.Role;
import com.tesis.users.User;
import com.tesis.users.UserService;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private AccessTokenRepository accessTokenRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("asndajsdnakjsndjkansdjksandjaksndakjsdhasjkkjasdjkajhkdskjsndjkasndkandaksjdnajksdnakdnsajdna"));
    private AuthenticationServiceImp authenticationService;

    @BeforeEach
    public void setUp() {
        authenticationService = new AuthenticationServiceImp(accessTokenRepository, userService, passwordEncoder, key);
    }

    @DisplayName("Authentication service - login() user not found")
    @Test
    public void login1() {

        ClientCredentialsBody body = ClientCredentialsBody.builder()
                .email("test@test.com")
                .password("test")
                .build();

        when(userService.getUser("test@test.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authenticationService.login(body));
    }

    @DisplayName("Authentication service - login() passwords dont match")
    @Test
    public void login2() {

        ClientCredentialsBody body = ClientCredentialsBody.builder()
                .email("test@test.com")
                .password("test")
                .build();

        when(userService.getUser("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("test", "test")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authenticationService.login(body));
    }

    @DisplayName("Authentication service - login() found valid token")
    @Test
    public void login3() {

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .build();

        AccessToken mockedToken = AccessToken.builder()
                .userId(1L)
                .token(getValidToken())
                .build();

        ClientCredentialsBody body = ClientCredentialsBody.builder()
                .email("test@test.com")
                .password("test")
                .build();

        when(userService.getUser("test@test.com")).thenReturn(Optional.of(mockedUser));
        when(passwordEncoder.matches("test", "test")).thenReturn(true);
        when(accessTokenRepository.findById(1L)).thenReturn(Optional.of(mockedToken));

        AccessToken token = authenticationService.login(body);
        assertNotNull(token);
        assertNotNull(token.getToken());
    }

    @DisplayName("Authentication service - login() expired token should retrieve a new one")
    @Test
    public void login4() {

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .build();

        AccessToken mockedToken = AccessToken.builder()
                .userId(1L)
                .token(getExpiredToken())
                .build();

        ClientCredentialsBody body = ClientCredentialsBody.builder()
                .email("test@test.com")
                .password("test")
                .build();

        when(userService.getUser("test@test.com")).thenReturn(Optional.of(mockedUser));
        when(passwordEncoder.matches("test", "test")).thenReturn(true);
        when(accessTokenRepository.findById(1L)).thenReturn(Optional.of(mockedToken));

        AccessToken token = authenticationService.login(body);
        assertNotNull(token);
        assertNotNull(token.getToken());
    }

    @DisplayName("Authentication service - login() token not found should retrieve a new one")
    @Test
    public void login5() {

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .role(
                        Role.builder()
                        .id(1L)
                        .privileges(
                                Sets.newHashSet(
                                        Privilege.builder().id(1L).name("GET_CLIENT").build(),
                                        Privilege.builder().id(2L).name("CREATE_CLIENT").build()
                                )
                        )
                        .build()
                )
                .build();

        ClientCredentialsBody body = ClientCredentialsBody.builder()
                .email("test@test.com")
                .password("test")
                .build();

        when(userService.getUser("test@test.com")).thenReturn(Optional.of(mockedUser));
        when(passwordEncoder.matches("test", "test")).thenReturn(true);
        when(accessTokenRepository.findById(1L)).thenReturn(Optional.empty());

        AccessToken token = authenticationService.login(body);
        assertNotNull(token);
        assertNotNull(token.getToken());
    }

    @DisplayName("Authentication service - logout() token expired")
    @Test
    public void logout1() {

        assertThrows(BadRequestException.class, () -> authenticationService.logout(getExpiredToken()));
    }

    @DisplayName("Authentication service - logout() token with no user claim")
    @Test
    public void logout2() {

        assertThrows(BadRequestException.class, () -> authenticationService.logout(getTokenWithNoUserClaim()));
    }

    @DisplayName("Authentication service - logout() token not found for user")
    @Test
    public void logout3() {

        when(accessTokenRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authenticationService.logout(getTokenWithNoUserClaim()));
    }

    @DisplayName("Authentication service - logout() ok")
    @Test
    public void logout4() {

        when(accessTokenRepository.findById(any())).thenReturn(Optional.of(AccessToken.builder().build()));
        doNothing().when(accessTokenRepository).delete(any());
        assertDoesNotThrow(() -> authenticationService.logout(getValidToken()));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() invalid token")
    @Test
    public void validatePrivilegesOnAccessToken1() {

        assertThrows(UnauthorizedException.class, () -> authenticationService.validatePrivilegesOnAccessToken(getExpiredToken(), Collections.emptyList()));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() invalid token subject")
    @Test
    public void validatePrivilegesOnAccessToken2() {

        assertThrows(InternalServerErrorException.class, () -> authenticationService.validatePrivilegesOnAccessToken(getTokenWithInvalidSubject(), Collections.emptyList()));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() old token")
    @Test
    public void validatePrivilegesOnAccessToken3() {

        when(accessTokenRepository.findById(any())).thenReturn(Optional.of(AccessToken.builder().token("token").build()));
        assertThrows(UnauthorizedException.class, () -> authenticationService.validatePrivilegesOnAccessToken(getValidToken(), Collections.emptyList()));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() not privilege should only validate token")
    @Test
    public void validatePrivilegesOnAccessToken4() {

        when(accessTokenRepository.findById(any())).thenReturn(Optional.of(AccessToken.builder().token(getValidToken()).build()));
        assertDoesNotThrow(() -> authenticationService.validatePrivilegesOnAccessToken(getValidToken(), Collections.emptyList()));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() invalid privileges")
    @Test
    public void validatePrivilegesOnAccessToken5() {

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .role(
                        Role.builder()
                                .id(1L)
                                .privileges(
                                        Sets.newHashSet(
                                                Privilege.builder().id(1L).name("GET_CLIENT").build(),
                                                Privilege.builder().id(2L).name("CREATE_CLIENT").build()
                                        )
                                )
                                .build()
                )
                .build();

        when(accessTokenRepository.findById(any())).thenReturn(Optional.of(AccessToken.builder().token(getValidToken()).build()));
        when(userService.getUser(1L)).thenReturn(Optional.of(mockedUser));

        assertThrows(ForbiddenException.class, () -> authenticationService.validatePrivilegesOnAccessToken(getValidToken(), Lists.newArrayList("GET_CLIENT", "UPDATE_CLIENT")));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() ok")
    @Test
    public void validatePrivilegesOnAccessToken6() {

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .role(
                        Role.builder()
                                .id(1L)
                                .privileges(
                                        Sets.newHashSet(
                                                Privilege.builder().id(1L).name("GET_CLIENT").build(),
                                                Privilege.builder().id(2L).name("CREATE_CLIENT").build()
                                        )
                                )
                                .build()
                )
                .build();

        when(accessTokenRepository.findById(any())).thenReturn(Optional.of(AccessToken.builder().token(getValidToken()).build()));
        when(userService.getUser(1L)).thenReturn(Optional.of(mockedUser));

        assertDoesNotThrow(() -> authenticationService.validatePrivilegesOnAccessToken(getValidToken(), Lists.newArrayList("GET_CLIENT")));
    }

    String getTokenWithNoUserClaim() {
        return "eyJ0eXBlIjoiQkVBUkVSIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNTgxNTUxODM0LCJleHAiOjc4OTI4OTkwMzR9.VnNZuox7XOq2vQknY-YoSFUBev6CtKeIIl6l-eZDAjK8X9MWCMMG3aqHSFlihP2-uMUDYUzR3-DGOP8jiXVUiQ";
    }

    String getValidToken() {
        return "eyJ0eXBlIjoiQkVBUkVSIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiIxIiwiaWF0IjoxNTgxNjA4MjE3LCJleHAiOjc4OTI5NTU0MTcsInVzZXIiOnsiY3JlYXRlZEF0IjpudWxsLCJ1cGRhdGVkQXQiOm51bGwsImRlbGV0ZWRBdCI6bnVsbCwiaWQiOjEsInN0YXR1cyI6bnVsbCwicGFzc3dvcmQiOiJ0ZXN0Iiwicm9sZSI6eyJpZCI6MSwibmFtZSI6bnVsbCwicHJpdmlsZWdlcyI6W3siaWQiOjEsIm5hbWUiOiJHRVRfQ0xJRU5UIn0seyJpZCI6MiwibmFtZSI6IkNSRUFURV9DTElFTlQifV19LCJlbWFpbCI6InRlc3RAdGVzdC5jb20iLCJuYW1lIjoidGVzdCIsImxhc3ROYW1lIjpudWxsLCJkbmkiOm51bGwsImFkZHJlc3MiOm51bGwsInBob25lIjpudWxsfX0.WLhGMVeDf5jVygOxCY-PI1F3-BNpWEJjgwOMSLG_S70pBGZ2Is7XznKpjNVUqw_UAR0-ls-Boc2KMWN-lDrNJA";
    }

    String getExpiredToken() {
        return "eyJ0eXBlIjoiQkVBUkVSIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNTgxMzY4NDUwLCJleHAiOjE1ODEyODIwNTAsInVzZXIiOnsiY3JlYXRlZEF0IjpudWxsLCJ1cGRhdGVkQXQiOm51bGwsImRlbGV0ZWRBdCI6bnVsbCwiaWQiOjEsInN0YXR1cyI6bnVsbCwicGFzc3dvcmQiOiJ0ZXN0Iiwicm9sZSI6bnVsbCwiZW1haWwiOiJ0ZXN0QHRlc3QuY29tIiwibmFtZSI6InRlc3QiLCJsYXN0TmFtZSI6bnVsbCwiZG5pIjpudWxsLCJhZGRyZXNzIjpudWxsLCJwaG9uZSI6bnVsbH19.fQZAD6FNuxj0evO3MSJzGo9MO-Qbukk-9sU8Gof3Wt40U2-dG6Xv1SHGobBB44Ufh1tHdYKNMU-2_g5Ijo9kdg";
    }

    String getTokenWithInvalidSubject() {
        return "eyJ0eXBlIjoiQkVBUkVSIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiJzYXJhc2EiLCJpYXQiOjE1ODE2MDc0MDksImV4cCI6Nzg5Mjk1NDYwOSwidXNlciI6eyJjcmVhdGVkQXQiOm51bGwsInVwZGF0ZWRBdCI6bnVsbCwiZGVsZXRlZEF0IjpudWxsLCJpZCI6MSwic3RhdHVzIjpudWxsLCJwYXNzd29yZCI6InRlc3QiLCJyb2xlIjpudWxsLCJlbWFpbCI6InRlc3RAdGVzdC5jb20iLCJuYW1lIjoidGVzdCIsImxhc3ROYW1lIjpudWxsLCJkbmkiOm51bGwsImFkZHJlc3MiOm51bGwsInBob25lIjpudWxsfX0.kfYDwVqqvyXBY54BfPtjIQcEmuGoJlM2iL2LJZDvzkH7Y9CNKFfbp8ZAZsqO9_zpJbLdoQGke2TjpePUebtESA";
    }
}
