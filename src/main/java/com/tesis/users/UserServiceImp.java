package com.tesis.users;

import com.google.common.base.Strings;
import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.NotFoundException;
import com.tesis.roles.Role;
import com.tesis.roles.RoleService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImp(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent()) {
            throw new NotFoundException(String.format("User %d not found", id));
        }

        return user.get();
    }

    @Override
    public User getUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException(String.format("User with email %s not found", email));
        }

        return user;
    }

    @Override
    public User createUser(UserRequestBody userRequestBody) {

        if (userRepository.findByEmail(userRequestBody.getEmail()) != null) {
            throw new BadRequestException(String.format("Email %s is already in use", userRequestBody.getEmail()));
        }

        Role role;
        try {
            role = roleService.getByName(userRequestBody.getRole());
        } catch (NotFoundException e) {
            throw new BadRequestException(String.format("Could not create user with invalid role %s", userRequestBody.getRole()));
        }

        User user = User.builder()
                .name(userRequestBody.getName())
                .lastName(userRequestBody.getLastName())
                .dni(userRequestBody.getDni())
                .email(userRequestBody.getEmail())
                .address(userRequestBody.getAddress())
                .phone(userRequestBody.getPhone())
                .role(role)
                .status(UserStatus.INACTIVE)
                .build();

        if (!Strings.isNullOrEmpty(userRequestBody.getPassword())) {
            user.setPassword(passwordEncoder.encode(userRequestBody.getPassword()));
        }

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            ConstraintViolationException error = (ConstraintViolationException) e.getCause();
            throw new BadRequestException(String.format("Invalid body, missing field [%s]", error.getConstraintName()));
        }

        return user;
    }

    @Override
    public User updateUser(Long userId, UserRequestBody userRequestBody) {
        //Revisar el tema de los permisos. Como está ahora, un user con role CLIENT se puede pasar a ADMIN.

        User user = getUser(userId);

        if (!user.getEmail().equals(userRequestBody.getEmail()) && userRepository.findByEmail(userRequestBody.getEmail()) != null) {
            throw new BadRequestException(String.format("Email %s is already in use", userRequestBody.getEmail()));
        }

        if (!Strings.isNullOrEmpty(userRequestBody.getRole())) {
            try {
                Role role = roleService.getByName(userRequestBody.getRole());
                user.setRole(role);
            } catch (NotFoundException e) {
                throw new BadRequestException(String.format("Could not update user with invalid role %s", userRequestBody.getRole()));
            }
        }

        if (!Strings.isNullOrEmpty(userRequestBody.getPassword())) {
            userRequestBody.setPassword(passwordEncoder.encode(userRequestBody.getPassword()));
        }

        user.merge(userRequestBody);
        userRepository.save(user);

        return user;
    }

    @Override
    public void deleteUser(Long id) {

        User user = getUser(id);
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }
}
