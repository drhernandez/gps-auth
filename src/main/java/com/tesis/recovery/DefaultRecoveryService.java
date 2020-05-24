package com.tesis.recovery;

import com.google.common.collect.Lists;
import com.tesis.emails.EmailService;
import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.InternalServerErrorException;
import com.tesis.exceptions.UnauthorizedException;
import com.tesis.users.User;
import com.tesis.users.UserRequestBody;
import com.tesis.users.UserService;
import com.tesis.utils.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Key;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class DefaultRecoveryService implements RecoveryService {

    private final RecoveryRepository recoveryRepository;
    private final UserService userService;
    private final EmailService emailService;
    private Key secretKey;

    @Autowired
    public DefaultRecoveryService(RecoveryRepository recoveryRepository, @Lazy UserService userService, EmailService emailService, Key secretKey) {
        this.recoveryRepository = recoveryRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.secretKey = secretKey;
    }

    @Override
    public RecoveryToken createToken(String email) {

        Optional<User> user = userService.getUser(email);
        Long userId = user
                .orElseThrow(() -> new BadRequestException(String.format("Email %s is not registered", email)))
                .getId();

        RecoveryToken recoveryToken = recoveryRepository.findById(userId)
                .filter(token -> validateToken(token.getToken()))
                .orElseGet(() -> generateRandomToken(userId, Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusSeconds(60 * 30).toInstant())));

        emailService.sendRecoveryPasswordEmail(Lists.newArrayList(user.get().getEmail()), recoveryToken.getToken());

        return recoveryToken;
    }

    @Override
    public RecoveryToken createWelcomeToken(User user) {

        RecoveryToken recoveryToken = recoveryRepository.findById(user.getId())
                .filter(token -> validateToken(token.getToken()))
                .orElseGet(() -> generateRandomToken(user.getId(), Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMonths(1).toInstant())));

        emailService.sendWelcomePasswordEmail(
                Lists.newArrayList(user.getEmail()),
                user.getName(),
                recoveryToken.getToken());

        return recoveryToken;
    }

    @Override
    public boolean validateToken(String tokenString) {
        return JwtUtils.validateToken(tokenString, secretKey);
    }

    @Override
    @Transactional
    public void changeUserPassword(String token, String rawPassword) {

        Long userId = JwtUtils.getUserIdFromToken(token, secretKey);

        // Checkeo que el token que mande sea el vigente
        RecoveryToken recoveryToken = recoveryRepository.findById(userId)
                .filter(tokenFound -> tokenFound.getToken().equals(token))
                .orElseThrow(UnauthorizedException::new);

        try {
            UserRequestBody userRequestBody = UserRequestBody.builder()
                    .password(rawPassword)
                    .build();

            userService.updateUser(userId, userRequestBody);
            recoveryRepository.delete(recoveryToken);

        } catch (Exception e) {
            logger.error("[message: Error trying to update user password for user {}] [error: {}] [stacktrace: {}]", userId, e.getMessage(), e.getStackTrace());
            throw new InternalServerErrorException("internal error");
        }
    }

    private RecoveryToken generateRandomToken(Long userId, Date expirationDate) {
        String jws = Jwts.builder()
                .setHeaderParam("type", "RECOVERY")
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()))
                .setExpiration(expirationDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        RecoveryToken token = RecoveryToken.builder()
                .userId(userId)
                .token(jws)
                .build();

        recoveryRepository.save(token);

        return token;
    }
}
