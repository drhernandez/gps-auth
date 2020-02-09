package com.tesis.authentication;

import com.tesis.exceptions.UnauthorizedException;
import com.tesis.users.User;
import com.tesis.users.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class AuthenticationServiceImp implements AuthenticationService {

    @Value("${jwt.secret-key}")
    private String secretString;
    private final Key key;

    private final AccessTokenRepository accessTokenRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationServiceImp(AccessTokenRepository accessTokenRepository, UserService userService, PasswordEncoder passwordEncoder) {
        this.accessTokenRepository = accessTokenRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
    }

    @Override
    public AccessToken createAccessToken(User user) {

        String jws = Jwts.builder()
                .setHeaderParam("type", "BEARER")
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC)))
                .claim("user", user)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        AccessToken accessToken = AccessToken.builder()
                .userId(user.getId())
                .token(jws)
                .build();

        accessTokenRepository.save(accessToken);
        return accessToken;
    }

    @Override
    public AccessToken login(ClientCredentialsBody credentialsBody) {

        User user = userService.getUser(credentialsBody.getUserEmail());
        if (!passwordEncoder.matches(credentialsBody.getRawPassword(), user.getPassword())) {
            throw new UnauthorizedException();
        }

        Optional<AccessToken> optionalToken = accessTokenRepository.findById(user.getId());
        return optionalToken.filter(this::validateToken).orElseGet(() -> createAccessToken(user));
    }
}
