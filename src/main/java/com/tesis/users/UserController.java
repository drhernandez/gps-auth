package com.tesis.users;

import com.tesis.exceptions.NotFoundException;
import com.tesis.recovery.RecoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RecoveryService recoveryService;

    @Autowired
    public UserController(UserService userService, RecoveryService recoveryService) {
        this.userService = userService;
        this.recoveryService = recoveryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id).orElseThrow(() -> new NotFoundException(String.format("User %s not found", id))));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserRequestBody userRequestBody) {
        try {
            User user = userService.createUser(userRequestBody);
            recoveryService.createWelcomeToken(user.getEmail());
            return ResponseEntity.ok(user);
        }
        catch (Exception e){
            logger.error("Error al crear usuario. [message: {}] [cause: {}] [stackTrace: {}]",
                    e.getMessage(),
                    e.getCause(),
                    e.getStackTrace());
            userService.physicallyDeleteUser(userRequestBody.getEmail());

            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestBody userRequestBody) {
        return ResponseEntity.ok(userService.updateUser(id, userRequestBody));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
