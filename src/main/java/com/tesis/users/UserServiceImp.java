package com.tesis.users;

import com.google.common.collect.Sets;
import com.tesis.constants.UserStatus;
import com.tesis.exceptions.BadRequestException;
import com.tesis.exceptions.NotFoundException;
import com.tesis.roles.Role;
import com.tesis.roles.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    //Esto no me termina de cerrar. Se puede usar el updateUser()
    @Override
    public User updatePassword(Long userId, String plainPassword) {

        User user  = getUser(userId);
        user.setPassword(passwordEncoder.encode(plainPassword));
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            user.setStatus(UserStatus.ACTIVE);
        }

        return userRepository.save(user);
    }

    @Override
    public User createUser(CreateUserRequestBody userRequestBody) {

        if (userRepository.findByEmail(userRequestBody.getEmail()) != null) {
            throw new BadRequestException(String.format("Email %s is already in use", userRequestBody.getEmail()));
        }

        List<Role> roles = roleService.getAllByName(userRequestBody.getRoles());
        if (roles.size() != userRequestBody.getRoles().size()) {

            userRequestBody.getRoles().removeAll(
                    roles
                            .stream()
                            .map(Role::getName)
                            .collect(Collectors.toList())
            );
            throw new BadRequestException(String.format("Could not create user with invalid roles %s", Arrays.toString(userRequestBody.getRoles().toArray())));
        }

        return userRepository.save(
                User.builder()
                        .name(userRequestBody.getName())
                        .lastName(userRequestBody.getLastName())
                        .dni(userRequestBody.getDni())
                        .email(userRequestBody.getEmail())
                        .address(userRequestBody.getAddress())
                        .phone(userRequestBody.getPhone())
                        .roles(Sets.newHashSet(roles))
                        .status(UserStatus.INACTIVE)
                        .build()
        );
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public void deleteUser(User user) {

    }
}
