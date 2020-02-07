package com.tesis.users;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRequestBody {

    private String name;
    private String lastName;
    private String role;
    private String dni;
    private String email;
    private String password;
    private String address;
    private String phone;
    private String status;
}
