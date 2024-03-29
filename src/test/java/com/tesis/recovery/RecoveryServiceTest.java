package com.tesis.recovery;

import com.tesis.emails.EmailService;
import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.InternalServerErrorException;
import com.tesis.exceptions.NotFoundException;
import com.tesis.exceptions.UnauthorizedException;
import com.tesis.users.User;
import com.tesis.users.UserService;
import com.tesis.utils.JwtUtils;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, JMockitExtension.class})
public class RecoveryServiceTest {

    @Mock
    private RecoveryRepository recoveryRepository;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;
    @Spy
    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @InjectMocks
    private DefaultRecoveryService recoveryService;

    @DisplayName("Recovery service - createToken() invalid user email")
    @Test
    public void createToken1() {

        when(userService.getUser("test@test.com")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> recoveryService.createToken("test@test.com"));
    }

    @DisplayName("Recovery service - createToken() error sending mail")
    @Test
    public void createToken2() {

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

        RecoveryToken mockedToken = RecoveryToken.builder()
                .userId(1L)
                .token("token")
                .build();

        when(userService.getUser("test@test.com")).thenReturn(Optional.of(mockedUser));
        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(mockedToken));
        doThrow(ResponseStatusException.class).when(emailService).sendRecoveryPasswordEmail(anyList(), anyString());

        assertThrows(ResponseStatusException.class, () -> recoveryService.createToken("test@test.com"));
    }

    @DisplayName("Recovery service - createToken() existing token but expired, should create a new one")
    @Test
    public void createToken3() {

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

        RecoveryToken mockedToken = RecoveryToken.builder()
                .userId(1L)
                .token("token")
                .build();

        when(userService.getUser("test@test.com")).thenReturn(Optional.of(mockedUser));
        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(mockedToken));

        RecoveryToken token = recoveryService.createToken("test@test.com");
        assertNotNull(token);
        assertEquals(1L, token.getUserId());
    }

    @DisplayName("Recovery service - createToken() existing token, should not create a new one")
    @Test
    public void createToken4() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public boolean validateToken(String token, Key key) {
                return true;
            }

            @mockit.Mock
            public Long getUserIdFromToken(String accessToken, Key secretKey) {
                return 1L;
            }
        };

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .build();

        RecoveryToken mockedToken = RecoveryToken.builder()
                .userId(1L)
                .token("token")
                .build();

        when(userService.getUser("test@test.com")).thenReturn(Optional.of(mockedUser));
        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(mockedToken));

        RecoveryToken token = recoveryService.createToken("test@test.com");
        assertNotNull(token);
        assertEquals(1L, token.getUserId());
    }

    @DisplayName("Recovery service - createToken() ok")
    @Test
    public void createToken5() {

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .build();

        when(userService.getUser("test@test.com")).thenReturn(Optional.of(mockedUser));
        when(recoveryRepository.findById(1L)).thenReturn(Optional.empty());

        RecoveryToken token = recoveryService.createToken("test@test.com");
        assertNotNull(token);
        assertEquals(1L, token.getUserId());
    }

    @DisplayName("Recovery service - createWelcomeToken() error sending mail")
    @Test
    public void createWelcomeToken2() {

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

        RecoveryToken mockedToken = RecoveryToken.builder()
                .userId(1L)
                .token("token")
                .build();

        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(mockedToken));
        doThrow(ResponseStatusException.class).when(emailService).sendWelcomePasswordEmail(anyList(), anyString(), anyString());

        assertThrows(ResponseStatusException.class, () -> recoveryService.createWelcomeToken(mockedUser));
    }

    @DisplayName("Recovery service - createWelcomeToken() existing token but expired, should create a new one")
    @Test
    public void createWelcomeToken3() {

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

        RecoveryToken mockedToken = RecoveryToken.builder()
                .userId(1L)
                .token("token")
                .build();

        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(mockedToken));

        RecoveryToken token = recoveryService.createWelcomeToken(mockedUser);
        assertNotNull(token);
        assertEquals(1L, token.getUserId());
    }

    @DisplayName("Recovery service - createWelcomeToken() existing token, should not create a new one")
    @Test
    public void createWelcomeToken4() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public boolean validateToken(String token, Key key) {
                return true;
            }

            @mockit.Mock
            public Long getUserIdFromToken(String accessToken, Key secretKey) {
                return 1L;
            }
        };

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .build();

        RecoveryToken mockedToken = RecoveryToken.builder()
                .userId(1L)
                .token("token")
                .build();

        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(mockedToken));

        RecoveryToken token = recoveryService.createWelcomeToken(mockedUser);
        assertNotNull(token);
        assertEquals(1L, token.getUserId());
    }

    @DisplayName("Recovery service - createWelcomeToken() ok")
    @Test
    public void createWelcomeToken5() {

        User mockedUser = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .password("test")
                .build();

        when(recoveryRepository.findById(1L)).thenReturn(Optional.empty());
        doNothing().when(emailService).sendWelcomePasswordEmail(anyList(), anyString(), anyString());

        RecoveryToken token = recoveryService.createWelcomeToken(mockedUser);
        assertNotNull(token);
        assertEquals(1L, token.getUserId());
    }

    @DisplayName("Recovery service - changeUserPassword() tokens not found")
    @Test
    public void changeUserPassword1() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public Long getUserIdFromToken(String token, Key key) {
                return 1L;
            }
        };
        when(recoveryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> recoveryService.changeUserPassword("token", "pass"));
    }

    @DisplayName("Recovery service - changeUserPassword() tokens dont match")
    @Test
    public void changeUserPassword2() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public Long getUserIdFromToken(String token, Key key) {
                return 1L;
            }
        };
        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(RecoveryToken.builder().token("other token").build()));

        assertThrows(UnauthorizedException.class, () -> recoveryService.changeUserPassword("token", "pass"));
    }

    @DisplayName("Recovery service - changeUserPassword() invalid user")
    @Test
    public void changeUserPassword3() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public Long getUserIdFromToken(String token, Key key) {
                return 1L;
            }
        };
        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(RecoveryToken.builder().token("token").build()));
        when(userService.updateUser(anyLong(), any())).thenThrow(NotFoundException.class);

        assertThrows(InternalServerErrorException.class, () -> recoveryService.changeUserPassword("token", "pass"));
    }

    @DisplayName("Recovery service - changeUserPassword() ok")
    @Test
    public void changeUserPassword4() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public Long getUserIdFromToken(String token, Key key) {
                return 1L;
            }
        };
        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(RecoveryToken.builder().token("token").build()));

        assertDoesNotThrow(() -> recoveryService.changeUserPassword("token", "pass"));
    }

    @DisplayName("Validate token - token expired")
    @Test
    public void validateToken1() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public boolean validateToken(String token, Key key) {
                return false;
            }
        };

        assertFalse(recoveryService.validateToken("token"));
    }

    @DisplayName("Validate token - token not found")
    @Test
    public void validateToken2() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public boolean validateToken(String token, Key key) {
                return true;
            }

            @mockit.Mock
            public Long getUserIdFromToken(String accessToken, Key secretKey) {
                return 1L;
            }
        };

        when(recoveryRepository.findById(1L)).thenReturn(Optional.empty());
        assertFalse(recoveryService.validateToken("token"));
    }

    @DisplayName("Validate token - token does not match")
    @Test
    public void validateToken3() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public boolean validateToken(String token, Key key) {
                return true;
            }

            @mockit.Mock
            public Long getUserIdFromToken(String accessToken, Key secretKey) {
                return 1L;
            }
        };

        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(RecoveryToken.builder().token("other token").build()));
        assertFalse(recoveryService.validateToken("token"));
    }

    @DisplayName("Validate token - ok")
    @Test
    public void validateToken4() {

        new MockUp<JwtUtils>() {
            @mockit.Mock
            public boolean validateToken(String token, Key key) {
                return true;
            }

            @mockit.Mock
            public Long getUserIdFromToken(String accessToken, Key secretKey) {
                return 1L;
            }
        };

        when(recoveryRepository.findById(1L)).thenReturn(Optional.of(RecoveryToken.builder().token("token").build()));
        assertTrue(recoveryService.validateToken("token"));
    }
}
