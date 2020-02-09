package com.tesis.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Builder
@AllArgsConstructor
@Getter@Setter
public class ClientCredentialsBody {

    @NotNull
    private String userEmail;
    @NotNull
    private String rawPassword;
}
