package com.tesis.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AccessToken> login(@RequestBody ClientCredentialsBody credentialsBody) {
        return ResponseEntity.ok(authenticationService.login(credentialsBody));
    }

    @PostMapping("/logout")
    public ResponseEntity logout(@RequestHeader("x-access-token") String token) {
        authenticationService.logout(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity validateTokenAndPrivileges(@RequestHeader("x-access-token") String token, @RequestBody @Nullable List<String> privileges) {
        authenticationService.validatePrivilegesOnAccessToken(token, privileges);
        return ResponseEntity.ok().build();
    }
}
