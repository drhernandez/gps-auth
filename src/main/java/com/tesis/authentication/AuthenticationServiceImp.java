package com.tesis.authentication;

import com.tesis.exceptions.UnauthorizedException;
import com.tesis.users.User;
import com.tesis.users.UserService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class AuthenticationServiceImp implements AuthenticationService {

    private Key secretKey;
    private final AccessTokenRepository accessTokenRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationServiceImp(Key secretKey, AccessTokenRepository accessTokenRepository, UserService userService, PasswordEncoder passwordEncoder) {
        this.secretKey = secretKey;
        this.accessTokenRepository = accessTokenRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AccessToken login(ClientCredentialsBody credentialsBody) {

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
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusYears(200).toInstant()))
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
}
