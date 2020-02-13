package com.tesis.authentication;

import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.ForbiddenException;
import com.tesis.exceptions.InternalServerErrorException;
import com.tesis.exceptions.UnauthorizedException;
import com.tesis.privileges.Privilege;
import com.tesis.users.User;
import com.tesis.users.UserService;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthenticationServiceImp implements AuthenticationService {

    private final AccessTokenRepository accessTokenRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private Key secretKey;

    @Autowired
    public AuthenticationServiceImp(AccessTokenRepository accessTokenRepository, UserService userService, PasswordEncoder passwordEncoder, Key secretKey) {
        this.accessTokenRepository = accessTokenRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = secretKey;
    }

    @Override
    public AccessToken login(ClientCredentialsBody credentialsBody) throws UnauthorizedException {

        Optional<User> userOpt = userService.getUser(credentialsBody.getEmail());
        User user = userOpt.orElseThrow(UnauthorizedException::new);

        if (!passwordEncoder.matches(credentialsBody.getPassword(), user.getPassword())) {
            throw new UnauthorizedException();
        }

        Optional<AccessToken> token = accessTokenRepository.findById(user.getId());
        return token
                .filter(this::validateAccessTokenToken)
                .orElseGet(() -> createAccessToken(user));
    }

    @Override
    public void logout(String token) {

        Long userId = getUserIdFromToken(token);
        Optional<AccessToken> accessTokenOpt = accessTokenRepository.findById(userId);
        AccessToken accessToken = accessTokenOpt.orElseThrow(() -> new BadRequestException("Invalid access token"));
        accessTokenRepository.delete(accessToken);
    }

    @Override
    public void validatePrivilegesOnAccessToken(String token, List<String> privileges) throws UnauthorizedException, ForbiddenException {

        Long userId = getUserIdFromToken(token);

        //Valido que el token sea el último generado por el user
        Optional<AccessToken> accessTokenOpt = accessTokenRepository.findById(userId);
        accessTokenOpt
                .filter(activeToken -> activeToken.token.equals(token))
                .orElseThrow(() -> new UnauthorizedException("Invalid access token"));

        //Comparo los privilegios
        if (privileges != null && privileges.size() > 0) {

            User user = userService.getUser(userId).get();
            List<String> userPrivileges = user.getRole()
                    .getPrivileges()
                    .stream()
                    .map(Privilege::getName)
                    .collect(Collectors.toList());

            privileges.removeAll(userPrivileges);
            if (privileges.size() != 0) {
                throw new ForbiddenException("User is not allowed to perform those actions");
            }
        }
    }

    private boolean validateAccessTokenToken(AccessToken accessToken) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(accessToken.getToken());
            return true;

        } catch (JwtException ex) {
            accessTokenRepository.delete(accessToken);
            return false;
        }
    }

    private AccessToken createAccessToken(User user) {

        String jws = Jwts.builder()
                .setHeaderParam("type", "BEARER")
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusDays(1).toInstant()))
//                .setExpiration(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusYears(200).toInstant()))
                .claim("user", user)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        AccessToken accessToken = AccessToken.builder()
                .userId(user.getId())
                .token(jws)
                .build();

        accessTokenRepository.save(accessToken);
        return accessToken;
    }

    private Long getUserIdFromToken(String accessToken) {

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
