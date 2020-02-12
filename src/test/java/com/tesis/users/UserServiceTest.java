package com.tesis.users;

import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.NotFoundException;
import com.tesis.roles.Role;
import com.tesis.roles.RoleService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImp userService;

    @DisplayName("User service - getUser(Long id) entity not found")
    @Test
    public void getUser1() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        NotFoundException e = assertThrows(NotFoundException.class, () -> userService.getUser(1L));
        assertEquals("User 1 not found", e.getReason());
    }

    @DisplayName("User service - getUser(Long id) ok")
    @Test
    public void getUser2() {

        User mock = User.builder()
                .id(1L)
                .name("test")
                .lastName("test")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mock));

        User user = userService.getUser(1L);

        assertNotNull(user);
        assertEquals("test", user.getName());
    }

    @DisplayName("User service - getUser(String email) entity not found")
    @Test
    public void getUser3() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(null);
        Optional<User> user = userService.getUser("test@test.com");

        assertNotNull(user);
        assertFalse(user.isPresent());
    }

    @DisplayName("User service - getUser(String email) ok")
    @Test
    public void getUser4() {

        User mock = User.builder()
                .id(1L)
                .name("test")
                .lastName("test")
                .email("test@test.com")
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(mock);

        Optional<User> user = userService.getUser("test@test.com");

        assertTrue(user.isPresent());
        assertEquals("test", user.get().getName());
    }

    @DisplayName("User service - createUser() user not found")
    @Test
    public void createUser1() {

        UserRequestBody requestBody = UserRequestBody.builder().email("test@test.com").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(User.builder().build());

        BadRequestException e = assertThrows(BadRequestException.class, () -> userService.createUser(requestBody));
        assertEquals("Email test@test.com is already in use", e.getReason());
    }

    @DisplayName("User service - createUser() existing user")
    @Test
    public void createUser2() {

        UserRequestBody requestBody = UserRequestBody.builder()
                .email("test@test.com")
                .role("TEST")
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(null);
        when(roleService.getByName("TEST")).thenThrow(NotFoundException.class);

        BadRequestException e = assertThrows(BadRequestException.class, () -> userService.createUser(requestBody));
        assertEquals("Could not create user with invalid role TEST", e.getReason());
    }

    @DisplayName("User service - createUser() incomplete data")
    @Test
    public void createUser3() {

        UserRequestBody requestBody = UserRequestBody.builder()
                .email("test@test.com")
                .role("TEST")
                .build();

        Role role = Role.builder().name("TEST").build();

        ConstraintViolationException innerE = new ConstraintViolationException("error", new SQLException(), "name");
        DataIntegrityViolationException outterE = new DataIntegrityViolationException("error", innerE);

        when(userRepository.findByEmail("test@test.com")).thenReturn(null);
        when(roleService.getByName("TEST")).thenReturn(role);
        when(userRepository.save(any())).thenThrow(outterE);

        BadRequestException e = assertThrows(BadRequestException.class, () -> userService.createUser(requestBody));
        assertEquals("Invalid body, missing field [name]", e.getReason());
    }

    @DisplayName("User service - createUser() ok")
    @Test
    public void createUser4() {

        UserRequestBody requestBody = UserRequestBody.builder()
                .name("test")
                .lastName("test")
                .email("test@test.com")
                .password("test")
                .role("TEST")
                .build();

        Role role = Role.builder().name("TEST").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(null);
        when(roleService.getByName("TEST")).thenReturn(role);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed password");

        User user = userService.createUser(requestBody);
        assertNotNull(user);
        assertEquals("test", user.getName());
        assertEquals("hashed password", user.getPassword());
    }

    @DisplayName("User service - updateUser() user not found")
    @Test
    public void updateUser1() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class, () -> userService.updateUser(1L, null));
        assertEquals("User 1 not found", e.getReason());
    }

    @DisplayName("User service - updateUser() email already in use")
    @Test
    public void updateUser2() {

        User mock = User.builder()
                .id(1L)
                .name("test")
                .lastName("test")
                .email("test@test.com")
                .password("test")
                .role(
                        Role.builder()
                        .id(1L)
                        .name("TEST")
                        .build()
                )
                .build();

        UserRequestBody requestBody = UserRequestBody
                .builder()
                .email("test2@test.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mock));
        when(userRepository.findByEmail("test2@test.com")).thenReturn(new User());

        BadRequestException e = assertThrows(BadRequestException.class, () -> userService.updateUser(1L, requestBody));
        assertEquals("Email test2@test.com is already in use", e.getReason());
    }

    @DisplayName("User service - updateUser() invalid role")
    @Test
    public void updateUser3() {

        User mock = User.builder()
                .id(1L)
                .name("test")
                .lastName("test")
                .email("test@test.com")
                .password("test")
                .role(
                        Role.builder()
                                .id(1L)
                                .name("TEST")
                                .build()
                )
                .build();

        UserRequestBody requestBody = UserRequestBody
                .builder()
                .email("test2@test.com")
                .role("INVALID_ROLE")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mock));
        when(userRepository.findByEmail("test2@test.com")).thenReturn(null);
        when(roleService.getByName("INVALID_ROLE")).thenThrow(NotFoundException.class);

        BadRequestException e = assertThrows(BadRequestException.class, () -> userService.updateUser(1L, requestBody));
        assertEquals("Could not update user with invalid role INVALID_ROLE", e.getReason());
    }

    @DisplayName("User service - updateUser() ok")
    @Test
    public void updateUser4() {

        User mock = User.builder()
                .id(1L)
                .name("test")
                .lastName("test")
                .email("test@test.com")
                .password("test")
                .role(
                        Role.builder()
                                .id(1L)
                                .name("TEST")
                                .build()
                )
                .build();

        Role mockRole = Role.builder()
                .id(1L)
                .name("TEST2")
                .build();

        UserRequestBody requestBody = UserRequestBody
                .builder()
                .email("test2@test.com")
                .role("TEST2")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mock));
        when(userRepository.findByEmail("test2@test.com")).thenReturn(null);
        when(roleService.getByName("TEST2")).thenReturn(mockRole);

        User updatedUser = userService.updateUser(1L, requestBody);
        assertNotNull(updatedUser);
        assertEquals("test2@test.com", updatedUser.getEmail());
        assertEquals("TEST2", updatedUser.getRole().getName());
    }

    @DisplayName("User service - deleteUser() user not found")
    @Test
    public void deleteUser1() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class, () -> userService.updateUser(1L, null));
        assertEquals("User 1 not found", e.getReason());
    }

    @DisplayName("User service - deleteUser() ok")
    @Test
    public void deleteUser2() {

        User mock = User.builder()
                .id(1L)
                .name("test")
                .lastName("test")
                .email("test@test.com")
                .password("test")
                .role(
                        Role.builder()
                                .id(1L)
                                .name("TEST")
                                .build()
                )
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mock));

        try {
            userService.deleteUser(1L);
        } catch (Exception e) {
            fail("should not fail here");
        }
    }
}
