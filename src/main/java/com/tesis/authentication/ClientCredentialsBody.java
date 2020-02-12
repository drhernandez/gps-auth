package com.tesis.authentication;

import lombok.*;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ClientCredentialsBody {

    @NotNull
    private String email;
    @NotNull
    private String password;
}
