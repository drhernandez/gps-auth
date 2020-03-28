package com.tesis.recovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recovery")
public class RecoveryController {

    private final RecoveryService recoveryService;

    @Autowired
    public RecoveryController(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @PostMapping()
    public ResponseEntity<RecoveryToken> createRecoveryToken(@RequestBody CreateRecoveryTokenBody body) {
        recoveryService.createToken(body.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity validateToken(@RequestHeader("x-recovery-token") String token) {
        return recoveryService.validateToken(token) ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<RecoveryToken> changePassword(@RequestHeader("x-recovery-token") String token, @RequestBody ChangePasswordBody body) {
        recoveryService.changeUserPassword(token, body.getPassword());
        return ResponseEntity.ok().build();
    }
}
