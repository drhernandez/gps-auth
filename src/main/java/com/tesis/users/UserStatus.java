package com.tesis.users;

import com.tesis.exceptions.BadRequestException;

public enum UserStatus {

    ACTIVE,
    INACTIVE,
    DELETED;

    public static UserStatus fromName(String name) {
        try {
            return UserStatus.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("%s is not a valid status.", name));
        }
    }
}
