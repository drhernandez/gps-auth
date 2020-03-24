package com.tesis.users;

import java.util.Optional;

public interface UserService {

    /**
     * Get user by id
     * @param id
     * @return
     */
    Optional<User> getUser(Long id);

    /**
     * Get user by email
     * @param email
     * @return
     */
    Optional<User> getUser(String email);

    /**
     * Create new user
     * @param userRequestBody
     * @return
     */
    User createUser(UserRequestBody userRequestBody);

    /**
     * Update User
     * @param userId
     * @param userRequestBody
     * @return
     */
    User updateUser(Long userId, UserRequestBody userRequestBody);

    /**
     * Delete existing user (only set status on deleted)
     * @param id
     */
    void deleteUser(Long id);

    /**
     * Delete physically existing user
     * @param id
     */
    void physicallyDeleteUser(Long id);
}
