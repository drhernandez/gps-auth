package com.tesis.users;

import com.tesis.exceptions.NotFoundException;
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

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<User> getUser(@Valid @RequestParam(value="email")  String email) {
        return ResponseEntity.ok(userService.getUser(email).orElseThrow(() -> new NotFoundException(String.format("User with email %s not found", email))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id).orElseThrow(() -> new NotFoundException(String.format("User %s not found", id))));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserRequestBody userRequestBody) {
        return ResponseEntity.ok(userService.createUser(userRequestBody));
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
