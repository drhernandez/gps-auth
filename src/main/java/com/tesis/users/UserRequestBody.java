package com.tesis.users;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRequestBody {

    private String name;
    private String lastName;
    private String dni;
    private String email;
    private String password;
    private String address;
    private String phone;
    private String status;
    private List<String> roles;
}
