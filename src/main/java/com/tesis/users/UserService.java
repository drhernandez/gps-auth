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
    User createUser(UserRequestBody userRequestBody);

    /**
     * Update User
     * @param userId
     * @param userRequestBody
     * @return
     */
    User updateUser(Long userId, UserRequestBody userRequestBody);

    /**
     * Delete existing user
     * @param id
     */
    void deleteUser(Long id);
}
