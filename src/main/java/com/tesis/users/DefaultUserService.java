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

import java.util.Optional;

@Service
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DefaultUserService(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> getUser(Long id) {
        return Optional.ofNullable(userRepository.findByIdAndStatusIsNot(id, UserStatus.DELETED));
    }

    @Override
    public Optional<User> getUser(String email) {
        return Optional.ofNullable(userRepository.findByEmailAndStatusIsNot(email, UserStatus.DELETED));
    }

    @Override
    public User createUser(UserRequestBody userRequestBody) {

        if (getUser(userRequestBody.getEmail()).isPresent()) {
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

        User user = getUser(userId).orElseThrow(() -> new NotFoundException(String.format("User %s not found", userId)));

        if (userRequestBody.getEmail() != null &&
                !user.getEmail().equals(userRequestBody.getEmail()) &&
                getUser(userRequestBody.getEmail()).isPresent()) {
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

        User user = getUser(id).orElseThrow(() -> new NotFoundException(String.format("User %s not found", id)));
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }
}
