package com.tesis.roles;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RolePostRequest {

    private String name;
    private List<String> privileges;
}
