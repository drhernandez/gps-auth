package com.tesis.utils;

import com.tesis.exceptions.InternalServerErrorException;
import com.tesis.exceptions.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJws;
import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, JMockitExtension.class})
public class JwtUtilsTest {

    @Mock
    Key key;
    @Mock
    JwtParserBuilder parserBuilderMock;
    @Mock
    JwtParser parserMock;
    @Mock
    Jws<Claims> claimsMock;

    @DisplayName("JwtUtils - validateToken() token invalid")
    @Test
    public void validateToken1() {

        new MockUp<Jwts>() {
            @mockit.Mock
            public JwtParserBuilder parserBuilder() {
                return parserBuilderMock;
            }
        };

        when(parserBuilderMock.setSigningKey(key)).thenReturn(parserBuilderMock);
        when(parserBuilderMock.build()).thenReturn(parserMock);
        when(parserMock.parseClaimsJws("token")).thenThrow(ExpiredJwtException.class);

        assertFalse(JwtUtils.validateToken("token", key));
    }

    @DisplayName("JwtUtils - validateToken() ok")
    @Test
    public void validateToken2() {

        new MockUp<Jwts>() {
            @mockit.Mock
            public JwtParserBuilder parserBuilder() {
                return parserBuilderMock;
            }
        };

        when(parserBuilderMock.setSigningKey(key)).thenReturn(parserBuilderMock);
        when(parserBuilderMock.build()).thenReturn(parserMock);
        when(parserMock.parseClaimsJws("token")).thenReturn(null);

        assertTrue(JwtUtils.validateToken("token", key));
    }

    @DisplayName("JwtUtils - getUserIdFromToken() invalid token")
    @Test
    public void getUserIdFromToken1() {

        new MockUp<Jwts>() {
            @mockit.Mock
            public JwtParserBuilder parserBuilder() {
                return parserBuilderMock;
            }
        };

        when(parserBuilderMock.setSigningKey(key)).thenReturn(parserBuilderMock);
        when(parserBuilderMock.build()).thenReturn(parserMock);
        when(parserMock.parseClaimsJws("token")).thenThrow(ExpiredJwtException.class);

        assertThrows(UnauthorizedException.class, () -> JwtUtils.getUserIdFromToken("token", key));
    }

    @DisplayName("JwtUtils - getUserIdFromToken() invalid subject")
    @Test
    public void getUserIdFromToken2() {

        new MockUp<Jwts>() {
            @mockit.Mock
            public JwtParserBuilder parserBuilder() {
                return parserBuilderMock;
            }
        };

        Claims bodyMock = new DefaultClaims();
        bodyMock.setSubject("test");
        Jws<Claims> claimsMock = new DefaultJws<Claims>(null, bodyMock, null);

        when(parserBuilderMock.setSigningKey(key)).thenReturn(parserBuilderMock);
        when(parserBuilderMock.build()).thenReturn(parserMock);
        when(parserMock.parseClaimsJws("token")).thenReturn(claimsMock);

        assertThrows(InternalServerErrorException.class, () -> JwtUtils.getUserIdFromToken("token", key));
    }

    @DisplayName("JwtUtils - getUserIdFromToken() ok")
    @Test
    public void getUserIdFromToken3() {

        new MockUp<Jwts>() {
            @mockit.Mock
            public JwtParserBuilder parserBuilder() {
                return parserBuilderMock;
            }
        };

        Claims bodyMock = new DefaultClaims();
        bodyMock.setSubject("1");
        Jws<Claims> claimsMock = new DefaultJws<Claims>(null, bodyMock, null);

        when(parserBuilderMock.setSigningKey(key)).thenReturn(parserBuilderMock);
        when(parserBuilderMock.build()).thenReturn(parserMock);
        when(parserMock.parseClaimsJws("token")).thenReturn(claimsMock);

        assertEquals(1L, JwtUtils.getUserIdFromToken("token", key));
    }
}
