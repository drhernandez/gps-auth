package com.tesis.users;

public interface UserService {

    /**
     * Get user by id
     * @param id
     * @return
     */
    User getUser(Long id);

    /**
     * Create new user
     * @param userRequestBody
     * @return
     */
    User createUser(CreateUserRequestBody userRequestBody);

    /**
     * Set or update password through the recovery password flow
     * @param plainPassword
     * @return
     */
    User updatePassword(Long userId, String plainPassword);

    /**
     * Update User
     * @param user
     * @return
     */
    User updateUser(User user);

    /**
     * Delete existing user
     * @param user
     */
    void deleteUser(User user);
}
