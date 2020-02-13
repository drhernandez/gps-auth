package com.tesis.authentication;

import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.UnauthorizedException;
import com.tesis.users.User;
import com.tesis.users.UserService;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Key;
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
        authenticationService = new AuthenticationServiceImp(key, accessTokenRepository, userService, passwordEncoder);
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

        try {
            authenticationService.logout(getValidToken());
        } catch (Exception e) {
            fail("should not throw exception");
        }
    }

    String getTokenWithNoUserClaim() {
        return "eyJ0eXBlIjoiQkVBUkVSIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNTgxNTUxODM0LCJleHAiOjc4OTI4OTkwMzR9.VnNZuox7XOq2vQknY-YoSFUBev6CtKeIIl6l-eZDAjK8X9MWCMMG3aqHSFlihP2-uMUDYUzR3-DGOP8jiXVUiQ";
    }

    String getValidToken() {
        return "eyJ0eXBlIjoiQkVBUkVSIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNTgxNTMyODkyLCJleHAiOjc4OTI4ODAwOTIsInVzZXIiOnsiY3JlYXRlZEF0IjpudWxsLCJ1cGRhdGVkQXQiOm51bGwsImRlbGV0ZWRBdCI6bnVsbCwiaWQiOjEsInN0YXR1cyI6bnVsbCwicGFzc3dvcmQiOiJ0ZXN0Iiwicm9sZSI6bnVsbCwiZW1haWwiOiJ0ZXN0QHRlc3QuY29tIiwibmFtZSI6InRlc3QiLCJsYXN0TmFtZSI6bnVsbCwiZG5pIjpudWxsLCJhZGRyZXNzIjpudWxsLCJwaG9uZSI6bnVsbH19.UnVH-uA1C77NHQKxUkDAibM1GJQmFoPZnDkoYmQLRT-e4q0f8ekPv47c0KBrQI662OdgNDrkp4jMujSYaFY3Zg";
    }

    String getExpiredToken() {
        return "eyJ0eXBlIjoiQkVBUkVSIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNTgxMzY4NDUwLCJleHAiOjE1ODEyODIwNTAsInVzZXIiOnsiY3JlYXRlZEF0IjpudWxsLCJ1cGRhdGVkQXQiOm51bGwsImRlbGV0ZWRBdCI6bnVsbCwiaWQiOjEsInN0YXR1cyI6bnVsbCwicGFzc3dvcmQiOiJ0ZXN0Iiwicm9sZSI6bnVsbCwiZW1haWwiOiJ0ZXN0QHRlc3QuY29tIiwibmFtZSI6InRlc3QiLCJsYXN0TmFtZSI6bnVsbCwiZG5pIjpudWxsLCJhZGRyZXNzIjpudWxsLCJwaG9uZSI6bnVsbH19.fQZAD6FNuxj0evO3MSJzGo9MO-Qbukk-9sU8Gof3Wt40U2-dG6Xv1SHGobBB44Ufh1tHdYKNMU-2_g5Ijo9kdg";
    }
}
