package com.tesis.users;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CreateUserRequestBody {

    private String name;
    private String lastName;
    private String dni;
    private String email;
    private String address;
    private String phone;
    private List<String> roles;
}
