package com.tesis.authentication;

import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.ForbiddenException;
import com.tesis.exceptions.UnauthorizedException;
import com.tesis.privileges.Privilege;
import com.tesis.users.User;
import com.tesis.users.UserService;
import com.tesis.utils.JwtUtils;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultAuthenticationService implements AuthenticationService {

    private final AccessTokenRepository accessTokenRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private Key secretKey;

    @Autowired
    public DefaultAuthenticationService(AccessTokenRepository accessTokenRepository, UserService userService, PasswordEncoder passwordEncoder, Key secretKey) {
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

        return accessTokenRepository.findById(user.getId())
                .filter(token -> JwtUtils.validateToken(token.getToken(), secretKey))
                .orElseGet(() -> createAccessToken(user));
    }

    @Override
    public void logout(String token) {

        Long userId = JwtUtils.getUserIdFromToken(token, secretKey);
        Optional<AccessToken> accessTokenOpt = accessTokenRepository.findById(userId);
        AccessToken accessToken = accessTokenOpt.orElseThrow(() -> new BadRequestException("Invalid access token"));
        accessTokenRepository.delete(accessToken);
    }

    @Override
    public void validatePrivilegesOnAccessToken(String token, List<String> privileges) throws UnauthorizedException, ForbiddenException {

        Long userId = JwtUtils.getUserIdFromToken(token, secretKey);

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

    private AccessToken createAccessToken(User user) {

        String jws = Jwts.builder()
                .setHeaderParam("type", "BEARER")
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusDays(1).toInstant()))
                .claim("user", user)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        AccessToken accessToken = AccessToken.builder()
                .userId(user.getId())
                .token(jws)
                .build();

        if (accessTokenRepository.existsById(accessToken.getUserId())) {
            accessTokenRepository.deleteById(accessToken.getUserId());
        }

        accessTokenRepository.save(accessToken);
        return accessToken;
    }
}
