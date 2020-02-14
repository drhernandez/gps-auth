package com.tesis.authentication;

import com.google.common.collect.Sets;
import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.ForbiddenException;
import com.tesis.exceptions.UnauthorizedException;
import com.tesis.privileges.Privilege;
import com.tesis.roles.Role;
import com.tesis.users.User;
import com.tesis.users.UserService;
import com.tesis.utils.JwtUtils;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Key;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, JMockitExtension.class})
public class AuthenticationServiceTest {

    @Mock
    private AccessTokenRepository accessTokenRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @InjectMocks
    private DefaultAuthenticationService authenticationService;

    @BeforeEach
    public void setUp() {
        authenticationService = new DefaultAuthenticationService(accessTokenRepository, userService, passwordEncoder, key);
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
                .token("token")
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

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public boolean validateToken(String token, Key key) {
                return false;
            }
        };

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .build();

        AccessToken mockedToken = AccessToken.builder()
                .userId(1L)
                .token("expired token")
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

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public boolean validateToken(String token, Key key) {
                return false;
            }
        };

        assertThrows(UnauthorizedException.class, () -> authenticationService.logout("expired token"));
    }

    @DisplayName("Authentication service - logout() token not found for user")
    @Test
    public void logout3() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public Long getUserIdFromToken(String token, Key key) {
                return 1L;
            }
        };
        when(accessTokenRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authenticationService.logout("token"));
    }

    @DisplayName("Authentication service - logout() ok")
    @Test
    public void logout4() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public Long getUserIdFromToken(String token, Key key) {
                return 1L;
            }
        };
        when(accessTokenRepository.findById(1L)).thenReturn(Optional.of(AccessToken.builder().build()));
        doNothing().when(accessTokenRepository).delete(any());
        assertDoesNotThrow(() -> authenticationService.logout("token"));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() invalid token")
    @Test
    public void validatePrivilegesOnAccessToken1() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public boolean validateToken(String token, Key key) {
                return false;
            }
        };

        assertThrows(UnauthorizedException.class, () -> authenticationService.validatePrivilegesOnAccessToken("expired token", Collections.emptyList()));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() not privilege should only validate token")
    @Test
    public void validatePrivilegesOnAccessToken4() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public Long getUserIdFromToken(String token, Key key) {
                return 1L;
            }
        };
        when(accessTokenRepository.findById(1L)).thenReturn(Optional.of(AccessToken.builder().token("token").build()));
        assertDoesNotThrow(() -> authenticationService.validatePrivilegesOnAccessToken("token", Collections.emptyList()));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() invalid privileges")
    @Test
    public void validatePrivilegesOnAccessToken5() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public Long getUserIdFromToken(String token, Key key) {
                return 1L;
            }
        };

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

        when(accessTokenRepository.findById(1L)).thenReturn(Optional.of(AccessToken.builder().token("token").build()));
        when(userService.getUser(1L)).thenReturn(Optional.of(mockedUser));

        assertThrows(ForbiddenException.class, () -> authenticationService.validatePrivilegesOnAccessToken("token", Lists.newArrayList("GET_CLIENT", "UPDATE_CLIENT")));
    }

    @DisplayName("Authentication service - validatePrivilegesOnAccessToken() ok")
    @Test
    public void validatePrivilegesOnAccessToken6() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public Long getUserIdFromToken(String token, Key key) {
                return 1L;
            }
        };

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

        when(accessTokenRepository.findById(1L)).thenReturn(Optional.of(AccessToken.builder().token("token").build()));
        when(userService.getUser(1L)).thenReturn(Optional.of(mockedUser));

        assertDoesNotThrow(() -> authenticationService.validatePrivilegesOnAccessToken("token", Lists.newArrayList("GET_CLIENT")));
    }
}
