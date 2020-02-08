package com.tesis.roles;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RolePostRequest {

    private String name;
    @Builder.Default
    private List<String> privileges = new ArrayList<>();
}
