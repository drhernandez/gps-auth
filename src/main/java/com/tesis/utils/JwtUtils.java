package com.tesis.utils;

import com.tesis.exceptions.InternalServerErrorException;
import com.tesis.exceptions.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;

@Slf4j
public class JwtUtils {

    public static boolean validateToken(String token, Key key) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (JwtException ex) {
            return false;
        }
    }

    public static Long getUserIdFromToken(String accessToken, Key secretKey) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(accessToken);

            return Long.parseLong(claims.getBody().getSubject());

        } catch (JwtException e) {
            logger.error("[message: Invalid token] [error: {}] [stacktrace: {}]", e.getMessage(), e.getStackTrace());
            throw new UnauthorizedException();
        } catch (Exception e) {
            logger.error("[message: Could not parse token subject] [error: {}] [stacktrace: {}]", e.getMessage(), e.getStackTrace());
            throw new InternalServerErrorException("internal error");
        }
    }
}
