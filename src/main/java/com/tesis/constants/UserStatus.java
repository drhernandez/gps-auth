package com.tesis.constants;

import com.tesis.exceptions.BadRequestException;

import java.util.Arrays;

public enum UserStatus {

    ACTIVE,
    INACTIVE;

    public static UserStatus fromName(String name) {
        try {
            return UserStatus.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("%s is not a valid status.", name));
        }
    }
}
